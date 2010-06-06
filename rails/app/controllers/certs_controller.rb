class CertsController < ApplicationController
  
  def index
    @certs = Cert.find_by_address(params[:domain], params[:endpoint])

    respond_to do |format|
      format.atom
    end
  end
  
end
