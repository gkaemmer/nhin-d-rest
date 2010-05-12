class AddMessageUuidToStatus < ActiveRecord::Migration
  def self.up
    add_column :statuses, :message_uuid, :string
  end

  def self.down
    remove_column :statuses, :message_uuid
  end
end
