package com.xenoage.zong.musiclayout.spacer.beam;

import com.xenoage.zong.musiclayout.notation.BeamNotation;
import com.xenoage.zong.musiclayout.notation.chord.StemNotation;
import com.xenoage.zong.musiclayout.spacer.beam.stem.BeamedStems;
import com.xenoage.zong.musiclayout.spacing.BeamSpacing;
import com.xenoage.zong.musiclayout.spacing.SystemSpacing;
import lombok.val;

import static com.xenoage.utils.kernel.Range.range;
import static com.xenoage.utils.math.MathUtils.interpolateLinear;
import static com.xenoage.zong.musiclayout.SLP.slp;
import static com.xenoage.zong.musiclayout.spacer.beam.BeamSpacer.beamSpacer;
import static com.xenoage.zong.musiclayout.spacer.beam.placement.TwoStavesBeamPlacer.twoStavesBeamPlacer;
import static com.xenoage.zong.musiclayout.spacer.beam.slant.TwoStavesBeamSlanter.twoStavesBeamSlanter;

/**
 * Creates the {@link BeamSpacing} for a beam on two adjacent staves.
 *
 * First, the preferred slant is computed. Then, the length of the first and last stem
 * is computed. Finally, the lengths of the inner stems are interpolated.
 * 
 * @author Andreas Wenger
 */
public class TwoStavesBeamSpacer {
	
	static final TwoStavesBeamSpacer twoStavesBeamSpacer = new TwoStavesBeamSpacer();


	BeamSpacing compute(BeamNotation beam, SystemSpacing system) {

		//compute slant
		val slant = twoStavesBeamSlanter.compute(beam);

		//compute the ends of the first and last stem
		val chords = beamSpacer.getBeamChordSpacings(beam, system);
		val stems = BeamedStems.fromBeam(chords);
		int upperStaffIndex = beam.element.getUpperStaffIndex();
		val placement = twoStavesBeamPlacer.compute(slant, stems, system, upperStaffIndex);

		//adjust the stem lengths by interpolating
		//the end LPs of the stems have then to be relative to the beam's staff (the staff of the first chord)
		int beamStaffIndex = beam.mp.staff;
		float leftEndYMm = system.getYMm(placement.leftSlp);
		float rightEndYMm = system.getYMm(placement.rightSlp);
		for (int i : range(stems)) {
			float yMm = interpolateLinear(leftEndYMm, rightEndYMm, stems.leftXIs, stems.rightXIs, stems.get(i).xIs);
			float lp = system.getLp(beamStaffIndex, yMm);
			StemNotation stem = beam.chords.get(i).stem;
			if (stem != null) //it could be possible that there is no stem
				stem.endSlp = slp(beamStaffIndex, lp); //before, maybe the end LP was relative to another staff
		}
		
		return new BeamSpacing(beam, chords);
	}
	
}
