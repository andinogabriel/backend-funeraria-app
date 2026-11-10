-- Refresh-token reuse detection. Rotation keeps the hash of the token it replaced so a second
-- use of that value can be told apart from an unknown token: inside a short grace window it is
-- a benign race between tabs (409, the client retries with the new cookie); after it, the token
-- was stolen and the device session is revoked.
alter table refresh_tokens add column previous_token_hash varchar(64);
alter table refresh_tokens add column rotated_at timestamp(6) with time zone;

create unique index ux_refresh_tokens_previous_token_hash
    on refresh_tokens (previous_token_hash);
