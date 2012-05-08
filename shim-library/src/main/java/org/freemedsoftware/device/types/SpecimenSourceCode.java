/*
 * $Id$
 *
 * Authors:
 *      Jeff Buchbinder <jeff@freemedsoftware.org>
 *
 * FreeMED Electronic Medical Record / Practice Management System
 * Copyright (C) 1999-2012 FreeMED Software Foundation
 *
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 2 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin St, Suite 500, Boston, MA 02110, USA.
 */

package org.freemedsoftware.device.types;

public enum SpecimenSourceCode {

	ABS("ABS"), AMN("AMN"), ASP("ASP"), BPH("BPH"), BIFL("BIFL"), BLDA("BLDA"), BBL(
			"BBL"), BLDC("BLDC"), BPU("BPU"), BLDV("BLDV"), BON("BON"), BRTH(
			"BRTH"), BRO("BRO"), BRN("BRN"), CALC("CALC"), CDM("CDM"), CNL(
			"CNL"), CTP("CTP"), CSF("CSF"), CVM("CVM"), CVX("CVX"), COL("COL"), CLBD(
			"CLBD"), CNJT("CNJT"), CUR("CUR"), CYST("CYST"), DIAF("DIAF"), DOSE(
			"DOSE"), DRN("DRN"), DUFL("DUFL"), EAR("EAR"), EARW("EARW"), ELT(
			"ELT"), ENDC("ENDC"), ENDM("ENDM"), EOS("EOS"), RBC("RBC"), EYE(
			"EYE"), EXHLD("EXHLD"), FIB("FIB"), FLT("FLT"), FIST("FIST"), FLU(
			"FLU"), GAS("GAS"), GAST("GAST"), GEN("GEN"), GENC("GENC"), GENL(
			"GENL"), GENV("GENV"), HAR("HAR"), IHG("IHG"), IT("IT"), ISLT(
			"ISLT"), LAM("LAM"), WBC("WBC"), LN("LN"), LNA("LNA"), LNV("LNV"), LIQ(
			"LIQ"), LYM("LYM"), MAC("MAC"), MAR("MAR"), MEC("MEC"), MBLD("MBLD"), MLK(
			"MLK"), MILK("MILK"), NAIL("NAIL"), NOS("NOS"), ORH("ORH"), PAFL(
			"PAFL"), PAT("PAT"), PRT("PRT"), PLC("PLC"), PLAS("PLAS"), PLB(
			"PLB"), PLR("PLR"), PMN("PMN"), PPP("PPP"), PRP("PRP"), PUS("PUS"), RT(
			"RT"), SAL("SAL"), SEM("SEM"), SER("SER"), SKN("SKN"), SKM("SKM"), SPRM(
			"SPRM"), SPT("SPT"), SPTC("SPTC"), SPTT("SPTT"), STON("STON"), STL(
			"STL"), SWT("SWT"), SNV("SNV"), TEAR("TEAR"), THRT("THRT"), THRB(
			"THRB"), TISS("TISS"), TISG("TISG"), TLGI("TLGI"), TLNG("TLNG"), TISPL(
			"TISPL"), TSMI("TSMI"), TISU("TISU"), TUB("TUB"), ULC("ULC"), UMB(
			"UMB"), UMED("UMED"), URTH("URTH"), UR("UR"), URC("URC"), URT("URT"), URNS(
			"URNS"), USUB("USUB"), VOM("VOM"), BLD("BLD"), BDY("BDY"), WAT(
			"WAT"), WICK("WICK"), WND("WND"), WNDA("WNDA"), WNDE("WNDE"), WNDD(
			"WNDD"), XXX("XXX");

	private String txt = null;

	SpecimenSourceCode(String txt) {
		this.txt = txt;
	}

	@Override
	public String toString() {
		return this.txt;
	}

}
