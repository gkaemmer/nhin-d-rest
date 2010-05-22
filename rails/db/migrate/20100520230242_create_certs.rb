require 'active_record/fixtures'

class CreateCerts < ActiveRecord::Migration
  def self.up
    create_table :certs do |t|
      t.string :cert
      t.string :key
      t.string :scope
      t.integer :object_id

      t.timestamps
    end
    directory = File.join(File.dirname(__FILE__), "data")
    Fixtures.create_fixtures(directory, "certs")
  end

  def self.down
    drop_table :certs
  end
end
