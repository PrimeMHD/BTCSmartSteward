package mhd.com.btcsmartsteward.CommonUtils;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.zip.GZIPInputStream;

import okio.ByteString;

/**
 * Created by Mi HD on 2018/3/16.
 */

public class StringUtil {

    private static final int BUFFERSIZE = 100000;

    public StringUtil() {
    }

    /**
     * Gzip解压数据
     * @param gzipByteStr
     * @return
     */
    public String decompressForGzip(ByteString gzipByteStr) {

        byte[] t=gzipByteStr.toByteArray();
        try {
            ByteArrayOutputStream out = new ByteArrayOutputStream();
            ByteArrayInputStream in = new ByteArrayInputStream(t);
            GZIPInputStream gzip = new GZIPInputStream(in);
            byte[] buffer = new byte[BUFFERSIZE];
            int n = 0;
            while ((n = gzip.read(buffer, 0, buffer.length)) > 0) {
                out.write(buffer, 0, n);
            }
            gzip.close();
            in.close();
            out.close();
            return out.toString();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }


}
