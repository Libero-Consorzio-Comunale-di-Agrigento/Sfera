--liquibase formatted sql
--changeset esasdelli:COMPILE_ALL runAlways:true failOnError:false

begin
    utilitypackage.compile_all;
end;
/