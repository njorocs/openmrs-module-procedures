/**
 * The contents of this file are subject to the OpenMRS Public License Version 1.0 (the "License"); you may not use this
 * file except in compliance with the License. You may obtain a copy of the License at http://license.openmrs.org
 * <p/>
 * Software distributed under the License is distributed on an "AS IS" basis, WITHOUT WARRANTY OF ANY KIND, either
 * express or implied. See the License for the specific language governing rights and limitations under the License.
 * <p/>
 * Copyright (C) OpenMRS, LLC.  All Rights Reserved.
 */

package org.openmrs.module.kenyaemr.fragment.controller.program.mchms;

import org.joda.time.DateTime;
import org.joda.time.Weeks;
import org.openmrs.Encounter;
import org.openmrs.EncounterType;
import org.openmrs.Form;
import org.openmrs.Obs;
import org.openmrs.PatientProgram;
import org.openmrs.module.kenyacore.CoreUtils;
import org.openmrs.module.kenyaemr.Dictionary;
import org.openmrs.module.kenyaemr.metadata.MchMetadata;
import org.openmrs.module.kenyaemr.wrapper.Enrollment;
import org.openmrs.module.metadatadeploy.MetadataUtils;
import org.openmrs.ui.framework.annotation.FragmentParam;
import org.openmrs.ui.framework.fragment.FragmentModel;

import java.util.Date;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * MCH program enrollment fragment
 */
public class MchmsEnrollmentSummaryFragmentController {

    public String controller(@FragmentParam("patientProgram") PatientProgram patientProgram,
                             @FragmentParam(value = "encounter", required = false) Encounter encounter,
                             @FragmentParam("showClinicalData") boolean showClinicalData,
                             FragmentModel model) {

        Map<String, Object> dataPoints = new LinkedHashMap<String, Object>();
        dataPoints.put("Enrolled", patientProgram.getDateEnrolled());

        Enrollment enrollment = new Enrollment(patientProgram);

        Obs ancNoObs = enrollment.firstObs(Dictionary.getConcept(Dictionary.ANTENATAL_CASE_NUMBER));
        if (ancNoObs != null) {
            dataPoints.put("ANC No", ancNoObs.getValueNumeric().intValue());
        }

        EncounterType mchMsConsultation = MetadataUtils.existing(EncounterType.class, MchMetadata._EncounterType.MCHMS_CONSULTATION);
        Form delivery = MetadataUtils.existing(Form.class, MchMetadata._Form.MCHMS_DELIVERY);
        Encounter deliveryEncounter = enrollment.encounterByForm(mchMsConsultation, delivery);

        Obs lmpObs = enrollment.firstObs(Dictionary.getConcept(Dictionary.LAST_MONTHLY_PERIOD));
        if (lmpObs != null) {
            if (deliveryEncounter == null) {
                Weeks weeks = Weeks.weeksBetween(new DateTime(lmpObs.getValueDate()), new DateTime(new Date()));
                dataPoints.put("Gestation (weeks)", weeks.getWeeks());
                dataPoints.put("LMP", lmpObs.getValueDate());
                dataPoints.put("EDD (LMP)", CoreUtils.dateAddDays(lmpObs.getValueDate(), 280));
            }
        }

        Obs eddUsoundObs = enrollment.firstObs(Dictionary.getConcept(Dictionary.EXPECTED_DATE_OF_DELIVERY));
        if (eddUsoundObs != null) {
            if (deliveryEncounter == null) {
                dataPoints.put("EDD (Ultrasound)", eddUsoundObs.getValueDate());
            }
        }

        Obs gravidaObs = enrollment.firstObs(Dictionary.getConcept(Dictionary.GRAVIDA));
        if (gravidaObs != null) {
            dataPoints.put("Gravida", gravidaObs.getValueNumeric().intValue());
        }

        Obs parityTermObs = enrollment.firstObs(Dictionary.getConcept(Dictionary.PARITY_TERM));
        Obs parityAbortionObs = enrollment.firstObs(Dictionary.getConcept(Dictionary.PARITY_ABORTION));

        if (parityTermObs != null && parityAbortionObs != null) {
            dataPoints.put("Parity", parityTermObs.getValueNumeric().intValue() + " + " + parityAbortionObs.getValueNumeric().intValue());
        }

        model.put("dataPoints", dataPoints);
        return "view/dataPoints";
    }
}