class AddFromToMessages < ActiveRecord::Migration
  def self.up
    add_column :messages, :from_domain, :string
    add_column :messages, :from_endpoint, :string
  end

  def self.down
    remove_column :messages, :from_endpoint
    remove_column :messages, :from_domain
  end
end
