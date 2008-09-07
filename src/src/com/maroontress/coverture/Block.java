package com.maroontress.coverture;

import java.io.PrintWriter;
import java.util.ArrayList;

/**
   �ؿ�����դΥΡ��ɤȤʤ���ܥ֥�å��Ǥ���
*/
public final class Block {

    /** �֥�å��μ��̻ҤǤ��� */
    private int id;

    /** �֥�å��Υե饰�Ǥ��� */
    private int flags;

    /** �����륨�å��פΥꥹ�ȤǤ��� */
    private ArrayList<Arc> inArcs;

    /** �ֽФ륨�å��פΥꥹ�ȤǤ��� */
    private ArrayList<Arc> outArcs;

    /**
       Block is a call instrumenting site; does the call: �ؿ���Ƥӽ�
       ���֥�å��Ǥ��뤳�Ȥ򼨤��ޤ���
    */
    private boolean callSite;

    /**
       Block is a landing pad for longjmp or throw: ���å��ν�����
       catch�ޤ���setjmp()�Ǥ��뤳�Ȥ򼨤��ޤ���
    */
    private boolean nonLocalReturn;

    /** �֥�å����б����륽���������ɤιԥ���ȥ������Ǥ��� */
    private LineEntry[] lines;

    /**
       XML�ǥ֥�å�����Ϥ��ޤ���

       @param out ������
    */
    public void printXML(final PrintWriter out) {
	out.printf("<block id='%d' flags='0x%x' callSite='%b' "
		   + "nonLocalReturn='%b'>\n",
		   id, flags, callSite, nonLocalReturn);
	for (Arc arc : outArcs) {
	    out.printf("<arc destination='%d' fake='%b' onTree='%b' "
		       + "fallThrough='%b' callNonReturn='%b' "
		       + "nonLocalReturn='%b' />\n",
		       arc.getEnd().getId(), arc.isFake(), arc.isOnTree(),
		       arc.isFallThrough(), arc.isCallNonReturn(),
		       arc.isNonLocalReturn());
	}
	if (lines != null) {
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

    /**
       �֥�å����������ޤ���

       @param id �֥�å��μ��̻�
       @param flags �֥�å��Υե饰
    */
    public Block(final int id, final int flags) {
	this.id = id;
	this.flags = flags;
	inArcs = new ArrayList<Arc>();
	outArcs = new ArrayList<Arc>();
    }

    /**
       �ؿ���ƤӽФ��֥�å����ɤ��������ꤷ�ޤ���

       @param b �ؿ���ƤӽФ��֥�å��ξ���true�������Ǥʤ����
       false
    */
    public void setCallSite(final boolean b) {
	callSite = b;
    }

    /**
       catch�ޤ���setjmp()��ޤ�֥�å����ɤ��������ꤷ�ޤ���

       @param b catch�ޤ���setjmp()��ޤ�֥�å��ξ���true��������
       �ʤ����false
    */
    public void setNonLocalReturn(final boolean b) {
	nonLocalReturn = b;
    }

    /**
       ���Υ֥�å������륨�å����ɲä��ޤ���

       @param arc ���å�
    */
    public void addInArc(final Arc arc) {
	inArcs.add(arc);
    }

    /**
       ���Υ֥�å�����Ф륨�å����ɲä��ޤ���

       @param arc ���å�
    */
    public void addOutArc(final Arc arc) {
	outArcs.add(arc);
    }

    /**
       ���Υ֥�å��ιԥ���ȥ����������ꤷ�ޤ���

       @param lines �ԥ���ȥ������
    */
    public void setLines(final LineEntry[] lines) {
	this.lines = lines;
    }

    /**
       ���̻Ҥ�������ޤ���

       @return ���̻�
    */
    public int getId() {
	return id;
    }
}
