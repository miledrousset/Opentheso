/**
 * 
 */
package fr.cnrs.opentheso.bdd.tools;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.channels.Channels;
import java.nio.channels.FileChannel;
import java.nio.channels.ReadableByteChannel;
import java.nio.channels.WritableByteChannel;
import java.security.DigestInputStream;
import java.security.DigestOutputStream;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;

/**
 * @author Persee team
 */
public class FileUtilities {
	static final int BUFF_SIZE = 524288;
  
        public String getDate() {
            DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
//            Date currentDate = new Date();
//            return dateFormat.format(currentDate);
            java.util.Date date = new java.util.Date();
            java.sql.Date sqlDate = new java.sql.Date(date.getTime());            
            return dateFormat.format(sqlDate);
        }
        
        private java.sql.Date getCurrentSqlDate() {
            java.util.Date date = new java.util.Date();
            return new java.sql.Date(date.getTime());            
        }        
        
        public java.sql.Date getDateFromString(String date) {
            java.sql.Date sqlDate = null;
            try {
                java.util.Date date1 = new SimpleDateFormat("yyyy-MM-dd").parse(date);  
                sqlDate = new java.sql.Date(date1.getTime());
                return sqlDate;
            } catch (Exception e) {
            }
            // en cas d'erreur, on retourne la date du jour
            return getCurrentSqlDate();
        }        
        
	/**
	 * Class for computing MD5 on an outputstream
	 * @author vincent
	 */
	public static class MD5OutputStream extends OutputStream
	{
		final DigestOutputStream stream;
		
		/**
		 * Contructs a new <code>MD5OutputStream</code>
		 * @param output
		 */
		public MD5OutputStream(OutputStream output) 
		{
			super();
			DigestOutputStream out = null;
			try
			{
				out = new DigestOutputStream(output, MessageDigest.getInstance("MD5"));
			} catch (NoSuchAlgorithmException nsae)
			{
				nsae.printStackTrace();
			}
			out.on(true);
			stream = out;			
		}
		
		/**
		 * @return the MD5 of value passed
		 */
		public String computeMD5()
		{
			byte[] digest = stream.getMessageDigest().digest();
            StringBuffer sb = new StringBuffer();
            for (int index = 0; index < digest.length; index++) {
                String hex = Integer.toHexString(digest[index] & 0xff);
                if (hex.length() == 1) {
                    sb.append('0');
                }
                sb.append(hex);
            }
            return sb.toString();
		}

		/**
		 * @throws IOException
		 * @see java.io.FilterOutputStream#close()
		 */
		public void close() throws IOException
		{
			stream.close();
		}

		/**
		 * @param obj
		 * @return
		 * @see java.lang.Object#equals(java.lang.Object)
		 */
		public boolean equals(Object obj)
		{
			return stream.equals(obj);
		}

		/**
		 * @throws IOException
		 * @see java.io.FilterOutputStream#flush()
		 */
		public void flush() throws IOException
		{
			stream.flush();
		}

		/**
		 * @return
		 * @see java.lang.Object#hashCode()
		 */
		public int hashCode()
		{
			return stream.hashCode();
		}

		/**
		 * @param on
		 * @see java.security.DigestOutputStream#on(boolean)
		 */
		public void on(boolean on)
		{
			stream.on(on);
		}

		/**
		 * @return
		 * @see java.security.DigestOutputStream#toString()
		 */
		public String toString()
		{
			return stream.toString();
		}

		/**
		 * @param b
		 * @param off
		 * @param len
		 * @throws IOException
		 * @see java.security.DigestOutputStream#write(byte[], int, int)
		 */
		public void write(byte[] b, int off, int len) throws IOException
		{
			stream.write(b, off, len);
		}

		/**
		 * @param b
		 * @throws IOException
		 * @see java.io.FilterOutputStream#write(byte[])
		 */
		public void write(byte[] b) throws IOException
		{
			stream.write(b);
		}

		/**
		 * @param b
		 * @throws IOException
		 * @see java.security.DigestOutputStream#write(int)
		 */
		public void write(int b) throws IOException
		{
			stream.write(b);
		}
	}
	
	/**
	 * Class for computing MD5 on an inputstream
	 * @author vincent
	 */
	public static class MD5InputStream extends InputStream
	{
		final DigestInputStream stream;
		
		/**
		 * Contructs a new <code>MD5InputStream</code>
		 * @param output
		 */
		protected MD5InputStream(InputStream input) 
		{
			super();
			DigestInputStream in = null;
			try
			{
				in = new DigestInputStream(input, MessageDigest.getInstance("MD5"));
			} catch (NoSuchAlgorithmException nsae)
			{
				nsae.printStackTrace();
			}
			in.on(true);
			stream = in;
		}
		
		/**
		 * @return the MD5 of value readed
		 */
		public String computeMD5()
		{
			byte[] digest = stream.getMessageDigest().digest();
            StringBuffer sb = new StringBuffer();
            for (int index = 0; index < digest.length; index++) {
                String hex = Integer.toHexString(digest[index] & 0xff);
                if (hex.length() == 1) {
                    sb.append('0');
                }
                sb.append(hex);
            }
            return sb.toString();
		}

		/**
		 * @return
		 * @throws IOException
		 * @see java.io.FilterInputStream#available()
		 */
		public int available() throws IOException
		{
			return stream.available();
		}

		/**
		 * @throws IOException
		 * @see java.io.FilterInputStream#close()
		 */
		public void close() throws IOException
		{
			stream.close();
		}

		/**
		 * @param readlimit
		 * @see java.io.FilterInputStream#mark(int)
		 */
		public void mark(int readlimit)
		{
			stream.mark(readlimit);
		}

		/**
		 * @return
		 * @see java.io.FilterInputStream#markSupported()
		 */
		public boolean markSupported()
		{
			return stream.markSupported();
		}

		/**
		 * @param on
		 * @see java.security.DigestInputStream#on(boolean)
		 */
		public void on(boolean on)
		{
			stream.on(on);
		}

		/**
		 * @return
		 * @throws IOException
		 * @see java.security.DigestInputStream#read()
		 */
		public int read() throws IOException
		{
			return stream.read();
		}

		/**
		 * @param b
		 * @param off
		 * @param len
		 * @return
		 * @throws IOException
		 * @see java.security.DigestInputStream#read(byte[], int, int)
		 */
		public int read(byte[] b, int off, int len) throws IOException
		{
			return stream.read(b, off, len);
		}

		/**
		 * @param b
		 * @return
		 * @throws IOException
		 * @see java.io.FilterInputStream#read(byte[])
		 */
		public int read(byte[] b) throws IOException
		{
			return stream.read(b);
		}

		/**
		 * @throws IOException
		 * @see java.io.FilterInputStream#reset()
		 */
		public void reset() throws IOException
		{
			stream.reset();
		}

		/**
		 * @param n
		 * @return
		 * @throws IOException
		 * @see java.io.FilterInputStream#skip(long)
		 */
		public long skip(long n) throws IOException
		{
			return stream.skip(n);
		}

		/**
		 * @return
		 * @see java.security.DigestInputStream#toString()
		 */
		public String toString()
		{
			return stream.toString();
		}
		
		
	}
	
    /**
     * Buffered copy of input stream to a given file. Must be fast&furious.
     */
    public static void copyToFile(InputStream in, File to) throws IOException {

        ReadableByteChannel inCh = Channels.newChannel(in);
        try (FileChannel out = (new FileOutputStream(to)).getChannel()) {
			long lastpos = 0;
			long nbRead;
			while ((nbRead = out.transferFrom(inCh, lastpos, BUFF_SIZE))>= BUFF_SIZE) {
				lastpos += nbRead;
			}
        } finally {
            // Make sure all streams are flushed and/or closed.
            in.close(); // so close input
        }
    }
    
    /**
     * Buffered copy of input stream to a given file. Must be fast&furious.
     */
    public static void copyToFile(FileInputStream in, File to) throws IOException {
        FileChannel inCh = in.getChannel();
        try (FileOutputStream outStream = new FileOutputStream(to)) {
			FileChannel out = outStream.getChannel();
			out.transferFrom(inCh, 0, inCh.size());
			out.close();
        } finally {
            // Make sure all streams are flushed and/or closed.
            in.close(); // so close input
        }
    }
    
    /**
     * Buffered copy of file to given output stream. Must be fast&furious... Omg
     * it's not!
     */
    public static void copyToStream(String from, OutputStream out)
            throws IOException {
        WritableByteChannel outCh;
        outCh = Channels.newChannel(out);
        try {
        	FileInputStream inStream = new FileInputStream(from); 
            try {
                FileChannel in = inStream.getChannel();
            	in.transferTo(0, in.size(), outCh);
            } finally
            {
                inStream.close();
            }
        } finally 
        {
        	outCh.close(); // so close out
        }
    }
    
    /**
     * Deletes all files and subdirectories under <code>dir</code>. Returns
     * <code>true</code> if all deletions were successful. If a deletion
     * fails, the method stops attempting to delete and returns
     * <code>false</code>.
     */
    public static boolean deleteDir(File dir) {
        if (dir.isDirectory()) {
            String[] children = dir.list();
            for (int i = 0; i < children.length; i++) {
                boolean success = deleteDir(new File(dir, children[i]));
                if (!success) {
                    return false;
                }
            }
        }
        // The directory is now empty so delete it
        return dir.delete();
    }
    
    /**
     * Generate MD5 checksum for data from input stream.
     */
    public static String generateMD5(InputStream is) throws IOException 
    {
        MD5InputStream mdIn = new MD5InputStream(is);
        //md.reset();
        byte[] buffer = new byte[4096];
        while (mdIn.read(buffer) != -1);
        mdIn.close();
        return mdIn.computeMD5();
    }
    
    /**
     * Generate MD5 checksum for the given file.
     */
    public static String generateMD5(String filename)
            throws FileNotFoundException, IOException {
        InputStream fis = new FileInputStream(filename);
        return generateMD5(fis);
    }
    
    /**
     * Generate MD5 checksum for the given file.
     */
    public static String generateMD5(File file)
            throws FileNotFoundException, IOException {
        InputStream fis = new FileInputStream(file);
        return generateMD5(fis);
    }
    
    /**
     * Check if the MD5 checksums are identical.
     */
    public static boolean confrontMD5(byte[] sum1, byte[] sum2) {
        for (int i = 0; i < sum1.length; i++) {
            if (sum1[i] != sum2[i])
                return false;
        }
        return true;
    }
    
    /**
     * Buffered copy of input stream to a given file. Must be fast&furious.
     * @return MD5
     */
    public static String copyToFileMD5(InputStream in, File to) throws IOException {
        MD5InputStream md5In = new MD5InputStream(in);
        ReadableByteChannel md5Ch = Channels.newChannel(md5In);
        try (FileChannel outCh = (new FileOutputStream(to)).getChannel()) {
			long lastpos = 0;
			long nbRead;
			while ((nbRead = outCh.transferFrom(md5Ch, lastpos, BUFF_SIZE))>= BUFF_SIZE)
			{
				lastpos += nbRead;
			}
        	return md5In.computeMD5();
		} finally {
            // Make sure all streams are flushed and/or closed.
			md5Ch.close(); // so close md5In and in
        }
    }
}
