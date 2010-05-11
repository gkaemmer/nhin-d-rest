class StatusesController < ApplicationController
  skip_before_filter :verify_authenticity_token
  
  def show
    @status = Status.find_by_message_uuid(params[:message_id])
    return unless validate_ownership(@status.message)
    
    respond_to do |format|
      format.text { render :text => @status.status }
    end
  end
  
  def update
    @status = Status.find_by_message_uuid(params[:message_id])
    return unless validate_ownership(@status.message)
    @status.status = request.body.read
    
    respond_to do |format|
      if @status.save
        format.text { render :text => @status.status }
      else
        format.text { render :text=> @status.errors, :status => :not_acceptable }
      end
    end
  end
    
end
