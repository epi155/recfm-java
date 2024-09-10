package io.github.epi155.recfm.java;


import java.io.DataInputStream;
import java.io.EOFException;
import java.io.IOException;
import java.io.InputStream;
import java.util.LinkedList;
import java.util.List;
import java.util.zip.GZIPInputStream;

public class DumpInfo {
    public final String lab;
    public final int at;
    public final int len;

    public DumpInfo(String lab, int at, int len) {
        this.lab = lab;
        this.at = at;
        this.len = len;
    }

    public static List<DumpInfo> load(InputStream is) {
        List<DumpInfo> dil = new LinkedList<DumpInfo>();
        InputStream zis = null;
        try {
            zis = new GZIPInputStream(is);
            DataInputStream dis = null;
            try {
                dis = new DataInputStream(zis);
                while (true) {
                    String label = dis.readUTF();
                    int offset = dis.readInt();
                    int length = dis.readInt();
                    dil.add(new DumpInfo(label, offset, length));
                }
            } catch (EOFException e) {
                // nope
            } finally {
                if (dis != null)
                    dis.close();
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        } finally {
            if (zis != null) {
                try {
                    zis.close();
                } catch (IOException e) {
                    // nope
                }
            }
        }
        return dil;
    }
}
