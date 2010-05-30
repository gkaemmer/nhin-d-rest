class MessagesController < ApplicationController
  before_filter :require_user  
  
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
  
  # def create_remote
  #   client_certs = Cert.find_by_scope(:hisp)
  #   cert = client_certs && client_certs[0]
  #   @hisp = RemoteHISP.new

  # POST /messages
  # POST /messages.xml
  def create
    # return create_remote if Domain.remote? params[:domain]
    @message = Message.new(:raw_message => params[:message][:raw_message])
    return unless validate_ownership(@message)

    respond_to do |format|
      if @message.save
        flash[:notice] = 'Message was successfully created.'
        format.html { redirect_to(message_path(params[:domain], params[:endpoint], @message)) }
        format.all  { head :status => :created, :location => message_path(params[:domain], params[:endpoint], @message) }
      else
        format.html { render :action => "new" }
        format.all { render :text => @message.errors.full_messages.join('; '), :status => :not_acceptable }
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
  
  
end
