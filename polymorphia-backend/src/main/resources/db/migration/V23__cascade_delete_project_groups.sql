ALTER TABLE project_groups_animals DROP CONSTRAINT IF EXISTS fk_progroani_on_project_group;
ALTER TABLE project_groups_animals
    ADD CONSTRAINT fk_pga_project_group
        FOREIGN KEY (project_group_id) REFERENCES project_groups(id) ON DELETE CASCADE;

ALTER TABLE project_groups_project_variants DROP CONSTRAINT IF EXISTS fk_progroprovar_on_project_group;
ALTER TABLE project_groups_project_variants
    ADD CONSTRAINT fk_pgpv_project_group
        FOREIGN KEY (project_group_id) REFERENCES project_groups(id) ON DELETE CASCADE;