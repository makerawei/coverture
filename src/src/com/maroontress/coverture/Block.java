package com.maroontress.coverture;

import com.maroontress.gcovparser.AbstractArc;
import com.maroontress.gcovparser.AbstractBlock;
import com.maroontress.gcovparser.DefaultArc;
import com.maroontress.gcovparser.DefaultBlock;
import com.maroontress.gcovparser.LineEntry;
import com.maroontress.gcovparser.Solver;
import java.io.PrintWriter;
import java.util.ArrayList;

/**
   �ؿ�����դΥΡ��ɤȤʤ���ܥ֥�å�����ݥ��饹�Ǥ���
*/
public final class Block extends AbstractBlock {

    /** �ѡ�����Ȥ��Ѵ����뤿��η����Ǥ��� */
    private static final double PERCENT = 100;

    /** */
    private DefaultBlock impl;

    /**
       �֥�å����������ޤ���

       @param id �֥�å��μ��̻�
       @param flags �֥�å��Υե饰
    */
    public Block(final int id, final int flags) {
	impl = new DefaultBlock(id, flags);
    }

    /**
       ���������������ޤ���

       @param end ���Υ֥�å�����Ф����������������֥�å�
       @param flags �ե饰
       @return ������
    */
    public DefaultArc createDefaultArc(final Block end, final int flags) {
	return new DefaultArc(impl, end.impl, flags);
    }

    /**
       ���ֹ���μ¹Բ���򥽡����ꥹ�Ȥ˥ޡ������ޤ���

       �����˼¹Բ���Υ�����Ȥ�ͭ���ˤʤäƤ���ɬ�פ�����ޤ���

       @param sourceList �������ꥹ��
    */
    public void addLineCounts(final SourceList sourceList) {
	assert (getCount() >= 0);
	long count = impl.getCount();
	LineEntry[] lines = impl.getLines();
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
	long count = impl.getCount();
	return (count == 0) ? 0 : PERCENT * c / count;
    }

    /**
       XML�ǥ֥�å�����Ϥ��ޤ���

       @param out ������
    */
    public void printXML(final PrintWriter out) {
	final boolean countValid = impl.getCountValid();

	out.printf("<block id='%d' flags='0x%x' callSite='%b' "
		   + "callReturn='%b' nonLocalReturn='%b'",
		   impl.getId(), impl.getFlags(), impl.isCallSite(),
		   impl.isCallReturn(), impl.isNonLocalReturn());
	if (countValid) {
	    out.printf(" count='%d'", impl.getCount());
	}
	out.printf(">\n");

	ArrayList<? extends AbstractArc> outArcs = impl.getOutArcs();
	for (AbstractArc arc : outArcs) {
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
	LineEntry[] lines = impl.getLines();
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

    /** {@inheritDoc} */
    public long getCount() {
	return impl.getCount();
    }

    /** {@inheritDoc} */
    public void setLines(final LineEntry[] lines) {
	impl.setLines(lines);
    }

    /** {@inheritDoc} */
    public int getId() {
	return impl.getId();
    }

    /** {@inheritDoc} */
    public ArrayList<? extends AbstractArc> getInArcs() {
	return impl.getInArcs();
    }

    /** {@inheritDoc} */
    public ArrayList<? extends AbstractArc> getOutArcs() {
	return impl.getOutArcs();
    }

    /** {@inheritDoc} */
    public void presolve() {
	impl.presolve();
    }

    /** {@inheritDoc} */
    public void sortOutArcs() {
	impl.sortOutArcs();
    }

    /** {@inheritDoc} */
    public void validate(final Solver s) {
	impl.validate(s);
    }

    /** {@inheritDoc} */
    public void validateSides(final Solver s) {
	impl.validateSides(s);
    }
}
