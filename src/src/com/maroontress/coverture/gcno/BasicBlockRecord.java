package com.maroontress.coverture.gcno;

import com.maroontress.coverture.CorruptedFileException;
import com.maroontress.coverture.Tag;
import com.maroontress.coverture.UnexpectedTagException;
import java.io.IOException;
import java.nio.ByteBuffer;

/**
   基本ブロックレコードです。

   basic_block: header int32:flags*
*/
public final class BasicBlockRecord {

    /** フラグの配列です。 */
    private int[] flags;

    /**
       バイトバッファからBASIC_BLOCKレコードを入力してBASIC_BLOCKレコー
       ドを生成します。バイトバッファの位置はBASIC_BLOCKレコードの先頭
       の位置でなければなりません。成功した場合は、バイトバッファの位
       置はBASIC_BLOCKレコードの次の位置に移動します。

       @param bb バイトバッファ
       @throws IOException 入出力エラー
       @throws UnexpectedTagException 予期しないタグを検出
    */
    public BasicBlockRecord(final ByteBuffer bb)
	throws IOException, UnexpectedTagException {
	int tag = bb.getInt();
	int length = bb.getInt();
	int next = bb.position() + 4 * length;

	if (tag != Tag.BLOCK) {
	    String m = String.format("unexpected tag: 0x%x", tag);
	    throw new UnexpectedTagException(m);
	}
	flags = new int[length];
	for (int k = 0; k < length; ++k) {
	    flags[k] = bb.getInt();
	}
	bb.position(next);
    }

    /**
       フラグの配列を取得します。

       @return フラグの配列
    */
    public int[] getFlags() {
	return flags;
    }
}
