package com.maroontress.coverture;

import com.maroontress.gcovparser.AbstractBlock;
import com.maroontress.gcovparser.LineEntry;
import java.io.PrintWriter;
import java.util.ArrayList;

/**
   �ؿ�����դΥΡ��ɤȤʤ���ܥ֥�å��μ������饹�Ǥ���
*/
public final class Block extends AbstractBlock<Block, Arc> {

    /** �ѡ�����Ȥ��Ѵ����뤿��η����Ǥ��� */
    private static final double PERCENT = 100;

    /**
       �֥�å����������ޤ���

       @param id �֥�å��μ��̻�
       @param flags �֥�å��Υե饰
    */
    public Block(final int id, final int flags) {
	super(id, flags);
    }

    /**
       ���ֹ���μ¹Բ���򥽡����ꥹ�Ȥ˥ޡ������ޤ���

       �����˼¹Բ���Υ�����Ȥ�ͭ���ˤʤäƤ���ɬ�פ�����ޤ���

       @param sourceList �������ꥹ��
    */
    public void addLineCounts(final SourceList sourceList) {
	assert (getCount() >= 0);
	long count = getCount();
	LineEntry[] lines = getLines();
	if (lines == null) {
	    return;
	}
	// ����for�����LineEntry�˰ܤ���...
	// e.addLineCounts(sourcelist, count);
	for (LineEntry e : lines) {
	    String fileName = e.getFileName();
	    int[] nums = e.getLines();
	    if (nums.length == 0) {
		continue;
	    }
	    Source source = sourceList.getSource(fileName);
	    for (int k = 0; k < nums.length; ++k) {
		source.addLineCount(nums[k], count);
	    }
	}
    }

    /**
       �¹Գ��ʥѡ�����ȡˤ�������ޤ���

       @param c �¹Բ��
       @return �¹Գ��
    */
    private double getRate(final long c) {
	long count = getCount();
	return (count == 0) ? 0 : PERCENT * c / count;
    }

    /**
       XML�ǥ֥�å�����Ϥ��ޤ���

       @param out ������
    */
    public void printXML(final PrintWriter out) {
	final boolean countValid = getCountValid();

	out.printf("<block id='%d' flags='0x%x' callSite='%b' "
		   + "callReturn='%b' nonLocalReturn='%b'",
		   getId(), getFlags(), isCallSite(),
		   isCallReturn(), isNonLocalReturn());
	if (countValid) {
	    out.printf(" count='%d'", getCount());
	}
	out.printf(">\n");

	ArrayList<Arc> outArcs = getOutArcs();
	for (Arc arc : outArcs) {
	    out.printf("<arc destination='%d' fake='%b' onTree='%b' "
		       + "fallThrough='%b' callNonReturn='%b' "
		       + "nonLocalReturn='%b' unconditional='%b'",
		       arc.getEnd().getId(), arc.isFake(), arc.isOnTree(),
		       arc.isFallThrough(), arc.isCallNonReturn(),
		       arc.isNonLocalReturn(), arc.isUnconditional());
	    if (countValid) {
		long c = arc.getCount();
		out.printf(" count='%d' rate='%.2f'", c, getRate(c));
	    }
	    out.printf("/>\n");
	}
	LineEntry[] lines = getLines();
	if (lines != null) {
	    // ����for�����LineEntry�˰ܤ���...
	    // e.printXML();
	    for (LineEntry e : lines) {
		String fileName = e.getFileName();
		int[] nums = e.getLines();
		if (nums.length == 0) {
		    continue;
		}
		out.printf("<lines fileName='%s'>\n", XML.escape(fileName));
		for (int k = 0; k < nums.length; ++k) {
		    out.printf("<line number='%d' />\n", nums[k]);
		}
		out.printf("</lines>\n");
	    }
	}
	out.printf("</block>\n");
    }
}
