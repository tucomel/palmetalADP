INSERT INTO ad_column(ad_column_id, ad_client_id, ad_org_id, isactive, created, updated, createdby, updatedby, name, description, help, version, entitytype, columnname, ad_table_id, ad_reference_id, ad_reference_value_id, ad_val_rule_id, fieldlength, defaultvalue, iskey, isparent, ismandatory, isupdateable, readonlylogic, isidentifier, seqno, istranslated, isencrypted, callout, vformat, valuemin, valuemax, isselectioncolumn, ad_element_id, ad_process_id, issyncdatabase, isalwaysupdateable, columnsql, mandatorylogic, infofactoryclass, isautocomplete, isallowlogging, formatpattern)
  VALUES(1106005, 0, 0, 'Y', '2011-09-21 10:25:57.0', '2011-09-21 10:26:17.0', 100, 100, 'Price includes Tax', 'Tax is included in the price ', 'The Tax Included checkbox indicates if the prices include tax.  This is also known as the gross price.', 0, 'LBRA', 'IsTaxIncluded', 1000007, 20, NULL, NULL, 1, 'N', 'N', 'N', 'Y', 'Y', NULL, 'N', 0, 'N', 'N', NULL, NULL, NULL, NULL, 'N', 1065, NULL, 'N', 'N', NULL, NULL, NULL, 'N', 'Y', NULL);
INSERT INTO ad_column(ad_column_id, ad_client_id, ad_org_id, isactive, created, updated, createdby, updatedby, name, description, help, version, entitytype, columnname, ad_table_id, ad_reference_id, ad_reference_value_id, ad_val_rule_id, fieldlength, defaultvalue, iskey, isparent, ismandatory, isupdateable, readonlylogic, isidentifier, seqno, istranslated, isencrypted, callout, vformat, valuemin, valuemax, isselectioncolumn, ad_element_id, ad_process_id, issyncdatabase, isalwaysupdateable, columnsql, mandatorylogic, infofactoryclass, isautocomplete, isallowlogging, formatpattern)
  VALUES(1106066, 0, 0, 'Y', '2011-11-18 15:48:22.0', '2011-11-18 15:48:22.0', 100, 100, 'Legal Message', 'Defines the Legal Message', 'Primary key table LBR_LegalMessage', 0, 'LBRA', 'LBR_LegalMessage_ID', 1000007, 30, NULL, NULL, 10, NULL, 'N', 'N', 'N', 'Y', NULL, 'N', 0, 'N', 'N', NULL, NULL, NULL, NULL, 'N', 1000253, NULL, 'N', 'N', NULL, NULL, NULL, 'N', 'Y', NULL);
INSERT INTO ad_column(ad_column_id, ad_client_id, ad_org_id, isactive, created, updated, createdby, updatedby, name, description, help, version, entitytype, columnname, ad_table_id, ad_reference_id, ad_reference_value_id, ad_val_rule_id, fieldlength, defaultvalue, iskey, isparent, ismandatory, isupdateable, readonlylogic, isidentifier, seqno, istranslated, isencrypted, callout, vformat, valuemin, valuemax, isselectioncolumn, ad_element_id, ad_process_id, issyncdatabase, isalwaysupdateable, columnsql, mandatorylogic, infofactoryclass, isautocomplete, isallowlogging, formatpattern)
  VALUES(1106067, 0, 0, 'Y', '2011-11-18 15:50:31.0', '2011-11-18 15:50:31.0', 100, 100, 'Tax Status', NULL, NULL, 0, 'LBRA', 'LBR_TaxStatus_ID', 1000007, 30, NULL, NULL, 10, NULL, 'N', 'N', 'N', 'Y', NULL, 'N', 0, 'N', 'N', NULL, NULL, NULL, NULL, 'N', 1106004, NULL, 'N', 'N', NULL, NULL, NULL, 'N', 'Y', NULL);

INSERT INTO ad_column_trl(ad_column_id, ad_language, ad_client_id, ad_org_id, isactive, created, createdby, updated, updatedby, name, istranslated)
  VALUES(1106005, 'pt_BR', 0, 0, 'Y', '2011-09-21 10:25:57.0', 100, '2011-09-21 10:25:57.0', 100, 'Price includes Tax', 'N');
INSERT INTO ad_column_trl(ad_column_id, ad_language, ad_client_id, ad_org_id, isactive, created, createdby, updated, updatedby, name, istranslated)
  VALUES(1106066, 'pt_BR', 0, 0, 'Y', '2011-11-18 15:48:22.0', 100, '2011-11-18 15:48:22.0', 100, 'Legal Message', 'N');
INSERT INTO ad_column_trl(ad_column_id, ad_language, ad_client_id, ad_org_id, isactive, created, createdby, updated, updatedby, name, istranslated)
  VALUES(1106067, 'pt_BR', 0, 0, 'Y', '2011-11-18 15:50:31.0', 100, '2011-11-18 15:50:31.0', 100, 'Tax Status', 'N');
