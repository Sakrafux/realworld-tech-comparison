-- Create trigger function for creation timestamp
-- Call after create table with `SELECT trigger_created_at('"<table>"');`

create or replace function set_created_at()
    returns trigger as
$$
begin
    NEW.created_at = now();
    NEW.updated_at = now();
    NEW.version = 0;
    return NEW;
end;
$$ language plpgsql;

create or replace function trigger_created_at(tablename regclass)
    returns void as
$$
begin
    execute format('CREATE TRIGGER set_created_at
        BEFORE INSERT
        ON %s
        FOR EACH ROW
    EXECUTE FUNCTION set_created_at();', tablename);
end;
$$ language plpgsql;

--------------------------------------------------------------------------------

-- Create trigger function for update timestamp
-- Call after create table with `SELECT trigger_updated_at('"<table>"');`

create or replace function set_updated_at()
    returns trigger as
$$
begin
    NEW.updated_at = now();
    NEW.version = OLD.version + 1;
    return NEW;
end;
$$ language plpgsql;

create or replace function trigger_updated_at(tablename regclass)
    returns void as
$$
begin
    execute format('CREATE TRIGGER set_updated_at
        BEFORE UPDATE
        ON %s
        FOR EACH ROW
        WHEN (OLD is distinct from NEW)
    EXECUTE FUNCTION set_updated_at();', tablename);
end;
$$ language plpgsql;
