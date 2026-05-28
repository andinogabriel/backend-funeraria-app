-- Per-item low-stock threshold used by the future LOW_STOCK_REACHED notification
-- (PR5b). The decision (option B from the design discussion) is to make the
-- threshold configurable per item, defaulting to 10. Existing rows backfill to
-- 10 automatically through the column default.
--
-- Why an integer (not BigDecimal): stock itself is tracked as Integer on the
-- items table (`stock INT`), and the threshold needs to compare directly against
-- it via a `stock <= threshold` predicate. Having both columns share the same
-- type avoids implicit casts and keeps the planner happy.
--
-- Why NOT NULL with a default: the threshold is a non-negotiable input to the
-- low-stock alert. Allowing NULL would force every consumer (notification
-- listener, dashboard panel, etc.) to coalesce on read, which is the kind of
-- spread-out responsibility this column was designed to eliminate.
--
-- No index: the threshold itself is never the search predicate. The future
-- notification listener iterates the items whose stock just changed and reads
-- the threshold from the same row in a single index lookup.

alter table items
    add column low_stock_threshold integer not null default 10;
