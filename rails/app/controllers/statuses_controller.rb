class StatusesController < ApplicationController
  def show
    # TODO: ceate find_by_message_uuid
    @status = Status.find_by_message_uuid(params[:message_id])
    
    respond_to do |format|
      format.text { render :text => @status.status }
    end
  end
end
