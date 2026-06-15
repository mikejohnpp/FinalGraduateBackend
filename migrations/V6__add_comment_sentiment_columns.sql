ALTER TABLE `FinalGraduateDB`.`comments`
    ADD COLUMN `sentiment`     VARCHAR(20)  NULL DEFAULT NULL,
    ADD COLUMN `confidence`    DOUBLE       NULL DEFAULT NULL,
    ADD COLUMN `cancel_reason` VARCHAR(100) NULL DEFAULT NULL;
