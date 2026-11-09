-- Explicit enrolment date ("fecha de alta") for affiliates.
--
-- The claim waiting period (ADR-0022) was computed from affiliates.start_date,
-- which is the @CreatedDate audit stamp: the day the row was typed in, not the
-- day the family joined. Back-dated and legacy affiliates therefore got wrong
-- WAITING_PERIOD verdicts. enrolment_date is the business date, editable on
-- create and update; start_date keeps meaning "record created".
--
-- Backfilled from start_date, the best date the system knows for existing rows.
-- The column default (the insert day) covers rows written without one, which
-- matches what start_date recorded until now.
alter table affiliates add column enrolment_date date not null default current_date;
update affiliates set enrolment_date = start_date where start_date is not null;
