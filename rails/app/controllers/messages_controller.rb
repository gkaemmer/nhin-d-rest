class MessagesController < ApplicationController
  
  before_filter :authenticate
  
  # TODO: Replace hardcoded security with User model
  USERS = [
    {:role => :edge, :user => "drjones@nhin.happyvalleypractice.example.org", :pw => "drjones_secret"},
    {:role => :edge, :user => "drsmith@nhin.sunnyfamilypractice.example.org", :pw => "drsmith_secret"},
    {:role => :hisp, :user => "hisp", :pw => "supersecret"}
  ]
  
  
  # GET /messages
  # GET /messages.xml
  def index
    # TODO: HISP should see only messages sent to HISP
    @messages = Message.find(:all, :conditions => ["to_endpoint = ? AND to_domain = ? ",params[:endpoint], params[:domain]])

    respond_to do |format|
      format.html # index.html.erb
      format.atom
    end
  end

  # GET /messages/1
  # GET /messages/1.xml
  def show
    # TODO: for HISP, validate HISP sent this message
    @message = Message.find_by_uuid(params[:id])

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

  # POST /messages
  # POST /messages.xml
  def create
    @message = Message.new(:raw_message => params[:message][:raw_message])

    respond_to do |format|
      if @message.save
        flash[:notice] = 'Message was successfully created.'
        format.html { redirect_to(message_path(params[:domain], params[:endpoint], @message)) }
        format.rfc822  { head :status => :created, :location => message_path(params[:domain], params[:endpoint], @message) }
      else
        format.html { render :action => "new" }
        format.rfc822 { head :status => :not_acceptable }
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
  def find_by_user(user)
    USERS.detect {|u| u[:user] == user}
  end
  
  def authenticate
    authenticate_or_request_with_http_basic do |user_name, password|
      u = find_by_user(user_name)
      
      return false unless u
      
      if u[:role] == :edge then
        endpoint, domain = user_name.split('@')
        return false unless (endpoint == params[:endpoint] && domain == params[:domain])
      end
      
      u[:user] == user_name && u[:pw] == password
    end
  end
  
end
