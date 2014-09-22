package com.xenoage.zong.io.musicxml.in.readers;

import static com.xenoage.utils.EnumUtils.getEnumValue;
import static com.xenoage.utils.NullUtils.notNull;
import static com.xenoage.utils.collections.CollectionUtils.alist;
import static com.xenoage.utils.iterators.It.it;
import static com.xenoage.utils.kernel.Range.range;
import static com.xenoage.utils.math.Fraction._0;
import static com.xenoage.utils.math.Fraction.fr;
import static com.xenoage.utils.math.MathUtils.clamp;
import static com.xenoage.zong.core.music.MeasureSide.Left;
import static com.xenoage.zong.core.music.MeasureSide.Right;
import static com.xenoage.zong.core.music.barline.Barline.barline;
import static com.xenoage.zong.core.music.barline.Barline.barlineBackwardRepeat;
import static com.xenoage.zong.core.music.barline.Barline.barlineForwardRepeat;
import static com.xenoage.zong.core.music.time.TimeType.timeSenzaMisura;
import static com.xenoage.zong.core.music.time.TimeType.timeType;
import static com.xenoage.zong.core.position.MP.atElement;
import static com.xenoage.zong.core.position.MP.atStaff;
import static com.xenoage.zong.core.text.UnformattedText.ut;
import static com.xenoage.zong.io.musicxml.in.readers.ChordReader.readChord;
import static com.xenoage.zong.io.musicxml.in.readers.FontInfoReader.readFontInfo;
import static com.xenoage.zong.io.musicxml.in.readers.OtherReader.readPosition;
import static com.xenoage.zong.io.musicxml.in.readers.OtherReader.readPositioning;
import static com.xenoage.zong.io.musicxml.in.util.CommandPerformer.execute;

import java.util.List;

import com.xenoage.utils.font.FontInfo;
import com.xenoage.utils.iterators.It;
import com.xenoage.utils.math.Fraction;
import com.xenoage.zong.commands.core.music.ColumnElementWrite;
import com.xenoage.zong.commands.core.music.MeasureAddUpTo;
import com.xenoage.zong.commands.core.music.MeasureElementWrite;
import com.xenoage.zong.commands.core.music.VoiceElementWrite;
import com.xenoage.zong.core.Score;
import com.xenoage.zong.core.format.Break;
import com.xenoage.zong.core.format.SystemLayout;
import com.xenoage.zong.core.header.ScoreHeader;
import com.xenoage.zong.core.music.ColumnElement;
import com.xenoage.zong.core.music.Measure;
import com.xenoage.zong.core.music.Part;
import com.xenoage.zong.core.music.Staff;
import com.xenoage.zong.core.music.Voice;
import com.xenoage.zong.core.music.barline.BarlineStyle;
import com.xenoage.zong.core.music.clef.Clef;
import com.xenoage.zong.core.music.clef.ClefType;
import com.xenoage.zong.core.music.direction.Coda;
import com.xenoage.zong.core.music.direction.Direction;
import com.xenoage.zong.core.music.direction.Dynamics;
import com.xenoage.zong.core.music.direction.DynamicsType;
import com.xenoage.zong.core.music.direction.NavigationMarker;
import com.xenoage.zong.core.music.direction.Pedal;
import com.xenoage.zong.core.music.direction.Pedal.Type;
import com.xenoage.zong.core.music.direction.Segno;
import com.xenoage.zong.core.music.direction.Tempo;
import com.xenoage.zong.core.music.direction.Wedge;
import com.xenoage.zong.core.music.direction.WedgeType;
import com.xenoage.zong.core.music.direction.Words;
import com.xenoage.zong.core.music.format.Position;
import com.xenoage.zong.core.music.format.Positioning;
import com.xenoage.zong.core.music.group.StavesRange;
import com.xenoage.zong.core.music.key.Key;
import com.xenoage.zong.core.music.key.TraditionalKey;
import com.xenoage.zong.core.music.key.TraditionalKey.Mode;
import com.xenoage.zong.core.music.layout.PageBreak;
import com.xenoage.zong.core.music.layout.SystemBreak;
import com.xenoage.zong.core.music.rest.Rest;
import com.xenoage.zong.core.music.time.Time;
import com.xenoage.zong.core.music.time.TimeType;
import com.xenoage.zong.core.position.MP;
import com.xenoage.zong.io.musicxml.in.util.MusicReaderException;
import com.xenoage.zong.musicxml.types.MxlAttributes;
import com.xenoage.zong.musicxml.types.MxlBackup;
import com.xenoage.zong.musicxml.types.MxlBarline;
import com.xenoage.zong.musicxml.types.MxlClef;
import com.xenoage.zong.musicxml.types.MxlCoda;
import com.xenoage.zong.musicxml.types.MxlDirection;
import com.xenoage.zong.musicxml.types.MxlDirectionType;
import com.xenoage.zong.musicxml.types.MxlDynamics;
import com.xenoage.zong.musicxml.types.MxlFormattedText;
import com.xenoage.zong.musicxml.types.MxlForward;
import com.xenoage.zong.musicxml.types.MxlInstrument;
import com.xenoage.zong.musicxml.types.MxlKey;
import com.xenoage.zong.musicxml.types.MxlMetronome;
import com.xenoage.zong.musicxml.types.MxlNormalTime;
import com.xenoage.zong.musicxml.types.MxlNote;
import com.xenoage.zong.musicxml.types.MxlPedal;
import com.xenoage.zong.musicxml.types.MxlPrint;
import com.xenoage.zong.musicxml.types.MxlScorePartwise;
import com.xenoage.zong.musicxml.types.MxlSegno;
import com.xenoage.zong.musicxml.types.MxlSound;
import com.xenoage.zong.musicxml.types.MxlStaffLayout;
import com.xenoage.zong.musicxml.types.MxlSystemLayout;
import com.xenoage.zong.musicxml.types.MxlTime;
import com.xenoage.zong.musicxml.types.MxlWedge;
import com.xenoage.zong.musicxml.types.MxlWords;
import com.xenoage.zong.musicxml.types.attributes.MxlPrintAttributes;
import com.xenoage.zong.musicxml.types.attributes.MxlPrintStyle;
import com.xenoage.zong.musicxml.types.attributes.MxlRepeat;
import com.xenoage.zong.musicxml.types.choice.MxlDirectionTypeContent;
import com.xenoage.zong.musicxml.types.choice.MxlDirectionTypeContent.MxlDirectionTypeContentType;
import com.xenoage.zong.musicxml.types.choice.MxlMusicDataContent;
import com.xenoage.zong.musicxml.types.choice.MxlMusicDataContent.MxlMusicDataContentType;
import com.xenoage.zong.musicxml.types.choice.MxlTimeContent.MxlTimeContentType;
import com.xenoage.zong.musicxml.types.enums.MxlBackwardForward;
import com.xenoage.zong.musicxml.types.enums.MxlRightLeftMiddle;
import com.xenoage.zong.musicxml.types.enums.MxlTimeSymbol;
import com.xenoage.zong.musicxml.types.groups.MxlLayout;
import com.xenoage.zong.musicxml.types.partwise.MxlMeasure;
import com.xenoage.zong.musicxml.types.partwise.MxlPart;

/**
 * This class reads the actual musical contents of
 * the given partwise MusicXML 2.0 document into a {@link Score}.
 * 
 * If possible, this reader works with the voice-element
 * to separate voices. TODO: if not existent or
 * used unreliably within a measure, implement this algorithm: 
 * http://archive.mail-list.com/musicxml/msg01673.html
 * 
 * TODO: Connect chords over staves, if they have the same
 * voice element but different staff element.
 *
 * @author Andreas Wenger
 */
public final class MusicReader {

	/**
	 * Reads the given MusicXML document and returns the score.
	 */
	public static void read(MxlScorePartwise doc, Score score, boolean ignoreErrors) {
		MusicReaderContext context = new MusicReaderContext(score, new MusicReaderSettings(
			ignoreErrors));
		
		//create the measures of the parts
		It<MxlPart> mxlParts = it(doc.getParts());
		for (MxlPart mxlPart : mxlParts) {
			//create measures
			execute(new MeasureAddUpTo(score, mxlPart.getMeasures().size()));
			//initialize each measure with a C clef
			Part part = score.getStavesList().getParts().get(mxlParts.getIndex());
			StavesRange stavesRange = score.getStavesList().getPartStaffIndices(part);
			for (int staff : stavesRange.getRange()) {
				execute(new MeasureElementWrite(new Clef(ClefType.clefTreble),
					score.getMeasure(MP.atMeasure(staff, 0)), _0));
			}
		}
		
		//write a 4/4 measure and C key signature in the first measure
		execute(new ColumnElementWrite(new Time(TimeType.time_4_4), score.getColumnHeader(0), _0, null));
		execute(new ColumnElementWrite(new TraditionalKey(0), score.getColumnHeader(0), _0, null));
			
		//read the parts
		mxlParts = it(doc.getParts());
		for (MxlPart mxlPart : mxlParts) {
			//clear part-dependent context values
			Part part = context.beginNewPart(mxlParts.getIndex());
			//read the measures
			It<MxlMeasure> mxlMeasures = it(mxlPart.getMeasures());
			for (MxlMeasure mxlMeasure : mxlMeasures) {
				try {
					context = readMeasure(context, mxlMeasure, mxlMeasures.getIndex());
				} catch (MusicReaderException ex) {
					throw new RuntimeException("Error at " + ex.getContext().toString(), ex);
				} catch (Exception ex) {
					throw new RuntimeException("Error (roughly) around " + context.toString(), ex);
				}
			}
		}

		//go through the whole score, and fill empty measures (that means, measures where
		//voice 0 has no single VoiceElement) with rests
		Fraction measureDuration = fr(1, 4);
		for (int iStaff = 0; iStaff < score.getStavesCount(); iStaff++) {
			Staff staff = score.getStaff(atStaff(iStaff));
			for (int iMeasure : range(staff.getMeasures())) {
				Measure measure = staff.getMeasure(iMeasure);
				Time newTime = score.getHeader().getColumnHeader(iMeasure).getTime();
				if (newTime != null) {
					//time signature has changed
					measureDuration = newTime.getType().getMeasureBeats();
				}
				if (measureDuration == null) { //senza misura
					measureDuration = fr(4, 4); //use whole rest
				}
				Voice voice0 = measure.getVoice(0);
				if (voice0.isEmpty()) {
					//TODO: "whole rests" or split. currently, also 3/4 rests are possible
					MP mp = atElement(iStaff, iMeasure, 0, 0);
					new VoiceElementWrite(score.getVoice(mp), mp, new Rest(measureDuration), null).execute();
				}
			}
		}
	}

	/**
	 * Reads the given measure element.
	 */
	private static MusicReaderContext readMeasure(MusicReaderContext context, MxlMeasure mxlMeasure,
		int measureIndex) {
		//begin a new measure
		context.beginNewMeasure(measureIndex);
		//list all elements
		List<MxlMusicDataContent> content = mxlMeasure.getMusicData().getContent();
		for (int i = 0; i < content.size(); i++) { //i may be modified within this loop
			MxlMusicDataContent mxlMDC = content.get(i);
			switch (mxlMDC.getMusicDataContentType()) {
				case Note: {
					MxlNote mxlNote = ((MxlNote) mxlMDC);
					//when it is a chord, ignore it, because we did already read it
					if (mxlNote.getContent().getFullNote().isChord()) {
						continue;
					}
					//instrument change?
					MxlInstrument mxlInstrument = mxlNote.getInstrument();
					if (mxlInstrument != null) {
						String instrumentID = mxlInstrument.getId();
						if (context.getInstrumentID() == null ||
							!context.getInstrumentID().equals(instrumentID)) {
							//instrument change detected!
							context.writeInstrumentChange(instrumentID);
						}
					}
					//collect all following notes which have a chord-element
					//inbetween there may be direction elements, so we collect the
					//notes until the first non-chord or non-direction element and after
					//that go on at the current position + 1
					List<MxlNote> mxlNotes = alist(mxlNote);
					for (int i2 = i + 1; i2 < content.size(); i2++) {
						MxlMusicDataContent mxlMDC2 = content.get(i2);
						boolean goOn = false;
						if (mxlMDC2.getMusicDataContentType() == MxlMusicDataContentType.Note) {
							MxlNote mxlNote2 = (MxlNote) mxlMDC2;
							if (mxlNote2.getContent().getFullNote().isChord()) {
								mxlNotes.add(mxlNote2);
								goOn = true;
							}
						}
						else if (mxlMDC2.getMusicDataContentType() == MxlMusicDataContentType.Direction) {
							goOn = true;
						}
						if (!goOn)
							break;
					}
					readChord(context, mxlNotes);
					break;
				}
				case Attributes:
					readAttributes(context, (MxlAttributes) mxlMDC);
					break;
				case Backup:
					readBackup(context, (MxlBackup) mxlMDC);
					break;
				case Forward:
					readForward(context, (MxlForward) mxlMDC);
					break;
				case Print:
					readPrint(context, (MxlPrint) mxlMDC);
					break;
				case Direction:
					readDirection(context, (MxlDirection) mxlMDC);
					break;
				case Barline:
					readBarline(context, (MxlBarline) mxlMDC);
					break;
			}
		}
		return context;
	}

	/**
	 * Reads the given attributes element.
	 */
	private static MusicReaderContext readAttributes(MusicReaderContext context,
		MxlAttributes mxlAttributes) {

		//divisions
		Integer divisions = mxlAttributes.getDivisions();
		if (divisions != null) {
			context.setDivisions(divisions);
		}

		//key signature
		MxlKey mxlKey = mxlAttributes.getKey();
		if (mxlKey != null) {
			//read fifths. currently, only -7 to 7 is supported (clamp, if needed)
			int mxlFifths = clamp(mxlKey.fifths, -7, 7);
			//write to column header (TODO: attribute "number" for single staves)
			Mode mode = getEnumValue(mxlKey.mode, Mode.values());
			Key key = new TraditionalKey(mxlFifths, mode);
			context.writeColumnElement(key);
		}

		//time signature
		MxlTime mxlTime = mxlAttributes.getTime();
		if (mxlTime != null) {
			Time time = null;
			MxlTimeContentType type = mxlTime.getContent().getTimeContentType();
			if (type == MxlTimeContentType.SenzaMisura) {
				//senza misura
				time = new Time(timeSenzaMisura);
			}
			else if (type == MxlTimeContentType.NormalTime) {
				//normal time
				MxlNormalTime mxlNormalTime = (MxlNormalTime) mxlTime.getContent();
				//common, cut or fractional?
				if (mxlTime.getSymbol() == MxlTimeSymbol.Cut)
					time = new Time(TimeType.timeAllaBreve);
				else if (mxlTime.getSymbol() == MxlTimeSymbol.Common)
					time = new Time(TimeType.timeCommon);
				else //otherwise, we currently support only normal fractional time signatures
					time = new Time(timeType(mxlNormalTime.getBeats(), mxlNormalTime.getBeatType()));
			}
			//write to column header (TODO: attribute "number" for single staves)
			if (time != null) {
				context.writeColumnElement(time);
			}
		}

		//clefs
		if (mxlAttributes.getClefs() != null) {
			for (MxlClef mxlClef : mxlAttributes.getClefs()) {
				ClefType clefType = ClefReader.readClef(mxlClef);
				Clef clef = (clefType != null ? new Clef(clefType) : null);
				//staff (called "number" in MusicXML), first staff is default
				int staff = mxlClef.getNumber() - 1;
				//add to staff
				if (clef != null) {
					context.writeMeasureElement(clef, staff);
				}
			}
		}

		/* TODO: transposition changes ~= instrument changes
		//transposition changes
		MxlTranspose mxlTranspose = mxlAttributes.getTranspose();
		if (mxlTranspose != null)
		{
			int chromatic = mxlTranspose.getChromatic();
			Transpose transpose = new Transpose(chromatic);
			//write to all staves of this part
			for (int staff = 0; staff < context.getPartStavesIndices().getCount(); staff++)
			{
				writeNoVoiceElement(transpose, staff);
		  }
		}
		*/

		return context;
	}

	/**
	 * Reads the given backup element.
	 */
	private static void readBackup(MusicReaderContext context, MxlBackup mxlBackup) {
		//duration
		Fraction duration = readDuration(context, mxlBackup.getDuration()).invert();
		//move cursor
		context.moveCurrentBeat(duration);
	}

	/**
	 * Reads the given forward element.
	 */
	private static void readForward(MusicReaderContext context, MxlForward mxlForward) {
		//duration
		Fraction duration = readDuration(context, mxlForward.getDuration());
		//move cursor
		context.moveCurrentBeat(duration);
	}

	/**
	 * Returns the duration as a {@link Fraction} from the given duration in divisions.
	 */
	public static Fraction readDuration(MusicReaderContext context, int duration) {
		if (duration == 0) {
			throw new RuntimeException("Element has a duration of 0.");
		}
		Fraction ret = fr(duration, 4 * context.getDivisions());
		return ret;
	}

	/**
	 * Reads the given print element.
	 */
	private static void readPrint(MusicReaderContext context, MxlPrint mxlPrint) {
		MxlLayout mxlLayout = mxlPrint.getLayout();
		ScoreHeader header = context.getScore().getHeader();

		//system and page break
		SystemBreak systemBreak = null;
		PageBreak pageBreak = null;
		MxlPrintAttributes mxlPA = mxlPrint.getPrintAttributes();
		if (mxlPA != null) {
			Boolean newSystem = mxlPA.getNewSystem();
			systemBreak = (newSystem == null ? null : (newSystem ? SystemBreak.NewSystem
				: SystemBreak.NoNewSystem));
			Boolean newPage = mxlPA.getNewPage();
			pageBreak = (newPage == null ? null : (newPage ? PageBreak.NewPage
				: PageBreak.NoNewPage));
			if (systemBreak != null || pageBreak != null) {
				context.writeColumnElement(new Break(pageBreak, systemBreak));
			}
		}

		//we assume that custom system layout information is just used in combination with
		//forced system/page breaks. so we ignore system-layout elements which are not combined
		//with system/page breaks.
		//the first measure of a score is also ok.
		if (context.getMp().measure == 0 || systemBreak == SystemBreak.NewSystem ||
			pageBreak == PageBreak.NewPage) {

			//first page or new page?
			boolean isPageBreak = pageBreak == PageBreak.NewPage;
			boolean isPageStarted = (context.getMp().measure == 0 || isPageBreak);
			if (isPageBreak) {
				//increment page index
				context.incPageIndex();
			}

			//first system or new system?
			boolean isSystemBreak = isPageBreak || systemBreak == SystemBreak.NewSystem;
			if (isSystemBreak) {
				//increment system index 
				context.incSystemIndex();
			}

			//read system layout, if there
			if (mxlLayout != null) {
				MxlSystemLayout mxlSystemLayout = mxlLayout.getSystemLayout();
				if (mxlSystemLayout != null) {
					SystemLayoutReader.Value sl = SystemLayoutReader
						.read(mxlSystemLayout, context.getTenthMm());
					SystemLayout systemLayout = sl.systemLayout;
	
					//for first systems on a page, use top-system-distance
					if (isPageStarted && sl.topSystemDistance != null) {
						systemLayout.setDistance(sl.topSystemDistance);
					}
	
					//apply values
					header.setSystemLayout(context.getSystemIndex(), systemLayout);
				}
			}
			
			//staff layouts
			if (mxlLayout != null) {
				for (MxlStaffLayout mxlStaffLayout : it(mxlLayout.getStaffLayouts())) {
					int staffIndex = mxlStaffLayout.getNumberNotNull() - 1;
					//get system layout. if it does not exist yet, create it
					SystemLayout systemLayout = header.getSystemLayout(context.getSystemIndex());
					if (systemLayout == null) {
						systemLayout = new SystemLayout();
						header.setSystemLayout(context.getSystemIndex(), systemLayout);
					}
					systemLayout.setStaffLayout(
						context.getPartStaffIndices().getStart() + staffIndex,
						StaffLayoutReader.readStaffLayout(mxlStaffLayout, context.getTenthMm()).staffLayout);
				}
			}

		}
	}

	/**
	 * Reads the given barline element.
	 * Currently only left and right barlines are supported.
	 */
	private static void readBarline(MusicReaderContext context, MxlBarline mxlBarline) {
		MxlRightLeftMiddle location = mxlBarline.getLocation();
		MxlRepeat repeat = mxlBarline.getRepeat();
		int measureIndex = context.getMp().measure;
		BarlineStyle style = null;
		if (mxlBarline.getBarStyle() != null)
			style = BarlineStyleReader.read(mxlBarline.getBarStyle().getBarStyle());
		if (repeat != null) {
			//repeat barline
			if (location == MxlRightLeftMiddle.Left) {
				//left barline
				if (repeat.getDirection() == MxlBackwardForward.Forward) {
					style = notNull(style, BarlineStyle.HeavyLight);
					new ColumnElementWrite(barlineForwardRepeat(style),
						context.getScore().getColumnHeader(measureIndex), null, Left).execute();
				}
			}
			else if (location == MxlRightLeftMiddle.Right) {
				//right barline
				if (repeat.getDirection() == MxlBackwardForward.Backward) {
					style = notNull(style, BarlineStyle.LightHeavy);
					int times = notNull(repeat.getTimes(), 1).intValue();
					new ColumnElementWrite(barlineBackwardRepeat(style, times),
						context.getScore().getColumnHeader(measureIndex), null, Right).execute();
				}
			}
		}
		else {
			//regular barline
			style = notNull(style, BarlineStyle.Regular);
			if (location == MxlRightLeftMiddle.Left) {
				//left barline
				new ColumnElementWrite(barline(style), context.getScore().getColumnHeader(measureIndex),
					null, Left).execute();
			}
			else if (location == MxlRightLeftMiddle.Right) {
				//right barline
				new ColumnElementWrite(barline(style), context.getScore().getColumnHeader(measureIndex),
					null, Right).execute();
			}
		}
	}

	//TIDY: move into own class, and read formatting info
	//TIDY: read print-style/positioning from all directions together (use common interface?)
	//TODO: add support for multiple direction-types within a single MusicXML direction
	/**
	 * Reads the given direction element.
	 */
	private static void readDirection(MusicReaderContext context,
		MxlDirection mxlDirection) {
		//staff
		int staff = notNull(mxlDirection.getStaff(), 1) - 1;

		//direction-types
		Direction direction = null;
		FontInfo defaultFont = context.getScore().getFormat().lyricFont;
		for (MxlDirectionType mxlType : mxlDirection.getDirectionTypes()) {
			MxlDirectionTypeContent mxlDTC = mxlType.getContent();
			MxlDirectionTypeContentType mxlDTCType = mxlDTC.getDirectionTypeContentType();
			switch (mxlDTCType) {
				case Coda: {
					//code
					MxlCoda mxlCoda = (MxlCoda) mxlDTC;
					MxlPrintStyle printStyle = notNull(mxlCoda.getPrintStyle(), MxlPrintStyle.empty);
					Positioning positioning = readPositioning(printStyle.getPosition(),
						null, mxlDirection.getPlacement(), context.getTenthMm(),
						context.getStaffLinesCount(staff));
					Coda coda = new Coda();
					coda.setPositioning(positioning);
					context.writeColumnElement(coda);
					break;
				}
				case Dynamics: {
					//dynamics
					MxlDynamics mxlDynamics = (MxlDynamics) mxlDTC;
					DynamicsType type = mxlDynamics.getElement();
					MxlPrintStyle printStyle = notNull(mxlDynamics.getPrintStyle(), MxlPrintStyle.empty);
					Positioning positioning = readPositioning(printStyle.getPosition(),
						mxlDynamics.getPlacement(), mxlDirection.getPlacement(), context.getTenthMm(),
						context.getStaffLinesCount(staff));
					Dynamics dynamics = new Dynamics(type);
					dynamics.setPositioning(positioning);
					context.writeMeasureElement(dynamics, staff);
					break;
				}
				case Metronome: {
					//metronome
					MxlMetronome mxlMetronome = (MxlMetronome) mxlDTC;
					FontInfo fontInfo = null;
					Position position = null;
					MxlPrintStyle mxlPrintStyle = mxlMetronome.getPrintStyle();
					if (mxlPrintStyle != null) {
						fontInfo = readFontInfo(mxlPrintStyle.getFont(), defaultFont);
						position = readPosition(mxlPrintStyle.getPosition(),
							context.getTenthMm(), context.getStaffLinesCount(staff));
					}
					direction = new Tempo(mxlMetronome.getBeatUnit().getDuration(), mxlMetronome.getPerMinute()); //text: TODO
					//direction.setFont(fontInfo); //TODO
					direction.setPositioning(position);
					break;
				}
				case Pedal: {
					//pedal
					MxlPedal mxlPedal = (MxlPedal) mxlDTC;
					Pedal.Type type = null;
					switch (mxlPedal.getType()) {
						case Start:
							type = Type.Start;
							break;
						case Stop:
							type = Type.Stop;
							break;
					}
					if (type != null) {
						Pedal pedal = new Pedal(type);
						pedal.setPositioning(readPosition(mxlPedal.getPrintStyle(),
							context.getTenthMm(), context.getStaffLinesCount(staff)));
						context.writeMeasureElement(pedal, staff);
					}
					break;
				}
				case Segno: {
					//segno
					MxlSegno mxlSegno = (MxlSegno) mxlDTC;
					MxlPrintStyle printStyle = notNull(mxlSegno.getPrintStyle(), MxlPrintStyle.empty);
					Positioning positioning = readPositioning(printStyle.getPosition(),
						null, mxlDirection.getPlacement(), context.getTenthMm(),
						context.getStaffLinesCount(staff));
					Segno segno = new Segno();
					segno.setPositioning(positioning);
					context.writeColumnElement(segno);
					break;
				}
				case Wedge: {
					//wedge
					MxlWedge mxlWedge = (MxlWedge) mxlDTC;
					int number = mxlWedge.getNumber();
					Position pos = readPosition(mxlWedge.getPosition(), context.getTenthMm(),
						context.getStaffLinesCount(staff));
					switch (mxlWedge.getType()) {
						case Crescendo:
							Wedge crescendo = new Wedge(WedgeType.Crescendo);
							crescendo.setPositioning(pos);
							context.writeMeasureElement(crescendo, staff);
							context.openWedge(number, crescendo);
							break;
						case Diminuendo:
							Wedge diminuendo = new Wedge(WedgeType.Diminuendo);
							diminuendo.setPositioning(pos);
							context.writeMeasureElement(diminuendo, staff);
							context.openWedge(number, diminuendo);
							break;
						case Stop:
							Wedge wedge = context.closeWedge(number);
							if (wedge == null) {
								if (false == context.getSettings().isIgnoringErrors())
									throw new RuntimeException("Wedge " + (number + 1) + " is not open!");
							}
							else
								context.writeMeasureElement(wedge.getWedgeEnd(), staff);
							break;
					}
					break;
				}
				case Words: {
					//words (currently only one element is supported)
					if (direction == null) {
						MxlWords mxlWords = (MxlWords) mxlDTC;
						MxlFormattedText mxlFormattedText = mxlWords.getFormattedText();
						direction = new Words(ut(mxlFormattedText.getValue()));
						
						MxlPrintStyle mxlPrintStyle = notNull(mxlFormattedText.getPrintStyle(), MxlPrintStyle.empty);
						Positioning positioning = readPositioning(mxlPrintStyle.getPosition(),
							mxlDirection.getPlacement(), null, context.getTenthMm(),
							context.getStaffLinesCount(staff));
						direction.setPositioning(positioning);
						
						//TODO
						//FontInfo fontInfo = readFontInfo(mxlPrintStyle.getFont(), defaultFont); 
						//direction.setFont(fontInfo);
					}
					break;
				}
			}
		}

		//sound for words: tempo
		MxlSound mxlSound = mxlDirection.getSound();
		if (mxlSound != null && mxlSound.getTempo() != null && direction instanceof Words) {
			Words words = (Words) direction;
			//always expressed in quarter notes per minute
			int quarterNotesPerMinute = mxlSound.getTempo().intValue();
			//convert words into a tempo direction
			direction = new Tempo(fr(1, 4), quarterNotesPerMinute); //TODO: words.getText()
			//direction.setFontInfo(words.getFontInfo()); //TODO
			direction.setPositioning(words.getPositioning());
		}

		//write direction to score
		//TODO: find out if measure direction or column direction.
		//currently, we write a column element only for tempo or navigation markers
		if (direction != null) {
			if (direction instanceof Tempo || direction instanceof NavigationMarker) {
				context.writeColumnElement((ColumnElement) direction);
			}
			else {
				context.writeMeasureElement(direction, staff);
			}
		}
	}

}
