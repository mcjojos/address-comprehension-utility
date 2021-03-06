CREATE TABLE COMPANIES
(
  ID INTEGER NOT NULL GENERATED ALWAYS AS IDENTITY (START WITH 1, INCREMENT BY 1),
  URL VARCHAR(2083) NOT NULL,
  NAME VARCHAR(50),
  ADDRESS_COUNT INTEGER NOT NULL DEFAULT 1
);

ALTER TABLE COMPANIES
ADD CONSTRAINT COMPANIES_PK Primary Key (ID);

CREATE TABLE ADDRESSES
(
  ID INTEGER NOT NULL GENERATED ALWAYS AS IDENTITY (START WITH 1, INCREMENT BY 1),
  ADDRESS VARCHAR(1000) NOT NULL,
  IMPORT_TIMESTAMP TIMESTAMP NOT NULL,
  COMPANY_ID INTEGER NOT NULL,
  ORDINAL INTEGER NOT NULL DEFAULT 1
);

ALTER TABLE ADDRESSES
ADD CONSTRAINT ADDRESSES_PK Primary Key (ID);

ALTER TABLE ADDRESSES
ADD CONSTRAINT COMPANIES_FK1 Foreign Key (COMPANY_ID)
REFERENCES COMPANIES (ID);
