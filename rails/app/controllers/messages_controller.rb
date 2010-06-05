class MessagesController < ApplicationController
  before_filter :require_user  
  hisp_actions :create
  
  # GET /messages
  # GET /messages.xml
  def index
    status = params[:status] || 'NEW'
    address = current_user && current_user.login
    @messages = Message.find_by_address_and_status(params[:domain], params[:endpoint], status, address)

    respond_to do |format|
      format.html # index.html.erb
      format.atom
    end
  end

  # GET /messages/1
  # GET /messages/1.xml
  def show
    @message = Message.find_by_uuid(params[:id])
    return unless validate_ownership(@message)

    respond_to do |format|
      format.html # show.html.erb
      format.rfc822 { render :text => @message.raw_message, :content_type => Mime::RFC822}
    end
  end

  # GET /messages/new
  # GET /messages/new.xml
  def new
    @message = Message.new

    respond_to do |format|
      format.html # new.html.erb
    end
  end

  # GET /messages/1/edit
  def edit
    @message = Message.find_by_uuid(params[:id])
  end
  
  def create_remote
    @hisp = remote_hisp
    loc = @hisp.create_message @message.signed_and_encrypted
    
    respond_to do |format|
      if loc
        format.all  { head :status => :created, :location => loc }
      else
        format.all { render :text => @hisp.response.body, :status => @hisp.response.code }
      end
    end
  end
        

  # POST /messages
  # POST /messages.xml
  def create
    @message = Message.new(:raw_message => params[:message][:raw_message])
    if  @message.parsed_message.to
      return create_remote if Domain.remote? Mail::Address.new(@message.parsed_message.to[0]).domain
    end
    return unless validate_message_security
    
    respond_to do |format|
      if @message.save
        flash[:notice] = 'Message was successfully created.'
        format.html { redirect_to(message_path(params[:domain], params[:endpoint], @message)) }
        format.all  { head :status => :created, :location => message_path(params[:domain], params[:endpoint], @message) }
      else
        format.html { render :action => "new" }
        format.all { render :text => @message.errors.full_messages.join('; '), :status => :bad_request }
      end
    end
  end

  # PUT /messages/1
  # PUT /messages/1.xml
  # Not supported for REST API, included only for testing purposes
  # TODO: Remove the PUT handling for production
  def update
    @message = Message.find_by_uuid(params[:id])
    @message.raw_message = params[:message][:raw_message]

    respond_to do |format|
      if @message.save
        flash[:notice] = 'Message was successfully updated.'
        format.html { redirect_to(message_path(params[:domain], params[:endpoint], @message)) }
      else
        format.html { render :action => "edit" }
      end
    end
  end

  # DELETE /messages/1
  # DELETE /messages/1.xml
  # Not supported for REST API, included only for testing purposes
  # TODO: Remove the DELETE handling for production
  def destroy
    @message = Message.find_by_uuid(params[:id])
    @message.destroy

    respond_to do |format|
      format.html { redirect_to(messages_path(params[:domain], params[:endpoint])) }
    end
  end
  
  private
  
  def validate_message_security
    return false unless validate_ownership(@message)
    return true unless current_role == :hisp
    if !@message.encrypted?
      head :status => :forbidden
      return false
    end
    m = Message.decrypt(@message.raw_message)
    if m.nil?
      head :status => :forbidden
      return false
    end
    unless m.signature_verified?
      head :status => :forbidden
      return flase
    end
    @message = m
    return true
  end
end
