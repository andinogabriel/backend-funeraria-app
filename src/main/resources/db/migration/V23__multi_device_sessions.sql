-- Multiple device sessions per user (ADR-0024). Until now user_devices.user_id was unique, so a
-- login from a second browser overwrote the first one's session. A session is now identified by
-- the user plus the client-generated device id; the JWT filter already looks it up that way.
alter table user_devices drop constraint user_devices_user_id_key;

create unique index ux_user_devices_user_device
    on user_devices (user_id, device_id);

-- What the active-sessions screen shows: the browser that opened the session and when.
alter table user_devices add column user_agent varchar(512);
alter table user_devices add column created_at timestamp(6) with time zone;

update user_devices set created_at = coalesce(last_seen_at, now()) where created_at is null;
