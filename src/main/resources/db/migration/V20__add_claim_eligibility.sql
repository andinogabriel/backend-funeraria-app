-- Claim eligibility (PR D of the affiliate-billing roadmap, ADR-0022).
--
-- An accidental death is covered from day one, without the waiting period of
-- the health tier, so the death-cause catalog says which causes are accidental.
-- The seeded "Accidente de transito" (id 4) is the only accidental one; admins
-- flag new causes through the death-cause endpoints.
alter table death_causes add column accidental boolean not null default false;
update death_causes set accidental = true where id = 4;

-- Verdict recorded when the deceased of a funeral is an affiliate: whether the
-- membership covered the claim on the death date, and why not (comma-separated
-- reason codes). A snapshot, so later payments or tariff edits do not rewrite
-- what the desk saw. Both stay NULL when the deceased was not an affiliate.
alter table deceased add column coverage_eligible boolean;
alter table deceased add column coverage_reasons varchar(200);
