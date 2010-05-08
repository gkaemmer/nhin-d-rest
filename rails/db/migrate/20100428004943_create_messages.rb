class CreateMessages < ActiveRecord::Migration
  def self.up
    create_table :messages do |t|
      t.string    :uuid, :null => false
      t.string    :to_domain
      t.string    :to_endpoint
      t.text      :raw_message
      t.timestamps
    end
    add_index :messages, :uuid, :unique => true
    add_index :messages, :to_domain
    add_index :messages, :to_endpoint
  end

  def self.down
    drop_table :messages
  end
end
