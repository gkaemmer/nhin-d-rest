# Be sure to restart your server when you modify this file.

# Your secret key for verifying cookie session data integrity.
# If you change this key, all old sessions will become invalid!
# Make sure the secret is at least 30 characters and all random, 
# no regular words or you'll be exposed to dictionary attacks.
ActionController::Base.session = {
  :key         => '_nhind_rest_session',
  :secret      => '3d13387c592f242926b34e155ab5705cf04def3ab19b1da9bdfb314228419a131df6439b28249e7721f262416edf20bb08f8a7d67958a68a50bb5ff0586d5c88'
}

# Use the database for sessions instead of the cookie-based default,
# which shouldn't be used to store highly confidential information
# (create the session table with "rake db:sessions:create")
# ActionController::Base.session_store = :active_record_store
