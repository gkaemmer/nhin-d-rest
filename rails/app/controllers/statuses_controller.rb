class StatusesController < ApplicationController
  skip_before_filter :verify_authenticity_token
  before_filter :require_user
  hisp_actions :update, :show
  
  
  def show_remote
    @hisp = remote_hisp
    status = @hisp.status params[:message_id]
    
    render :text => status
  end
  
  
  def show
    return show_remote if Domain.remote? params[:domain]

    @status = Status.find_by_message_uuid(params[:message_id])
    return unless validate_ownership(@status.message)
    
    respond_to do |format|
      format.text { render :text => @status.status }
    end
  end
  
  def update_remote
    @hisp = remote_hisp
    status = @hisp.update_status(params[:message_id], request.body.read)
    
    respond_to do |format|
      if status
        format.text { render :text => status }
      else
        format.text { head :status => @hisp.response.code }
      end
    end
  end
  
  def update
    return update_remote if Domain.remote? params[:domain]
    
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
