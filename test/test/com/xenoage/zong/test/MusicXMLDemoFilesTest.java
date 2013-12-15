package com.xenoage.zong.test;

import static com.xenoage.zong.musicxml.util.PlainMusicXMLFilenameFilter.plainMusicXMLFilenameFilter;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;

import org.junit.Test;

import com.xenoage.utils.jse.io.FileUtils;
import com.xenoage.utils.jse.xml.JseXmlReader;
import com.xenoage.utils.jse.xml.JseXmlWriter;
import com.xenoage.utils.xml.XmlException;
import com.xenoage.utils.xml.XmlReader;
import com.xenoage.zong.musicxml.MusicXMLDocument;

/**
 * Tests the loading of all MusicXML demo files.
 * 
 * @author Andreas Wenger
 */
public class MusicXMLDemoFilesTest {

	private String dirs[] = { "../shared/data/test/scores/musicxml11",
		"../shared/data/test/scores/musicxml20" };


	@Test public void testRead()
		throws Exception {
		test(false);
	}

	@Test public void testReadWriteRead()
		throws Exception {
		test(true);
	}

	private void test(boolean reload)
		throws Exception {
		long totalMusicXMLReadingTime = 0;
		long lastTime = 0;
		for (String dir : dirs) {
			for (File file : FileUtils.listFiles(new File(dir), plainMusicXMLFilenameFilter, false)) {
				System.out.println(file);
				lastTime = System.currentTimeMillis();
				XmlReader reader = new JseXmlReader(new FileInputStream(file));
				try {
					lastTime = System.currentTimeMillis();

					//load the document
					MusicXMLDocument doc = MusicXMLDocument.read(reader);

					if (reload) {
						//write the document into memory
						ByteArrayOutputStream bos = new ByteArrayOutputStream();
						doc.write(new JseXmlWriter(bos));
						//reload it from memory
						MusicXMLDocument.read(new JseXmlReader(new ByteArrayInputStream(bos.toByteArray())));
					}

					totalMusicXMLReadingTime += (System.currentTimeMillis() - lastTime);
				} catch (XmlException ex) {
					throw new Exception("Failed for " + dir + "/" + file.getName() + ": " + ex.getMessage(),
						ex);
				}
			}
		}
		//print time
		System.out.println("Total time for read" + (reload ? "/write/read: " : ": ") + totalMusicXMLReadingTime + " ms");
	}

}
