class User < ActiveRecord::Base
  acts_as_authentic do |config|
    config.validates_format_of_login_field_options :with => Authlogic::Regex.email
  end
end
