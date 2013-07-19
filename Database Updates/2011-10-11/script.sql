--- SAVED SEARCH

UPDATE savedsearch
	SET locale = LOWER(language)
	WHERE locale IS NULL;
	
ALTER TABLE savedsearch DROP COLUMN "language";
	
--- SAVED ITEMS UPDATES
	
UPDATE saveditem
	SET locale = LOWER(language)
	WHERE locale IS NULL;
	
UPDATE saveditem 
	SET europeanauri = (
		SELECT eid.europeanauri 
		FROM europeanaid AS eid 
		WHERE eid.id = europeanaid
	) 
	WHERE europeanaid IS NOT NULL;
	
DELETE FROM saveditem 
	WHERE europeanauri is null;
	--- ATTENTION: This statement is actually deleting users saved items, but these items are not containing valid references to existing objects.

ALTER TABLE saveditem DROP COLUMN "language";
ALTER TABLE saveditem DROP COLUMN "europeanaid";
ALTER TABLE saveditem DROP COLUMN "carouselitemid";

--- SEARCH TERM

UPDATE searchterm
	SET locale = LOWER(language)
	WHERE locale IS NULL;
	
ALTER TABLE searchterm DROP COLUMN "language";

--- SOCIAL TAG

UPDATE socialtag
	SET locale = LOWER(language)
	WHERE locale IS NULL;

UPDATE socialtag 
	SET europeanauri = (
		SELECT eid.europeanauri 
		FROM europeanaid AS eid 
		WHERE eid.id = europeanaid
	) 
	WHERE europeanaid IS NOT NULL;
	
ALTER TABLE socialtag DROP COLUMN "language";
ALTER TABLE socialtag DROP COLUMN "europeanaid";
