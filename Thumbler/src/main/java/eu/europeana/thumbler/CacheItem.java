package eu.europeana.thumbler;


import com.sun.tools.javac.util.Paths;

import java.io.*;
import java.util.ArrayList;

public class CacheItem extends CheckUrl{
    private FileTree fileTree;

    private String hashContent; // storage of org only once...
    private String hashUrl; // the generated imgs needs to be stored perl url refering them
    private String mimeType;
    private String fileType;
    private Integer orgW;
    private Integer orgH;

    ArrayList<File> createdFiles = new ArrayList<File>(); // all files created during processing of this record, remove them if final result is not ok

    public CacheItem(FileTree fileTree, String uri) {
        super(uri);
        initiateParams(fileTree);
    }
    public CacheItem(FileTree fileTree, String uri, Integer redirectDepth) {
        super(uri, redirectDepth);
        initiateParams(fileTree);
    }

    public boolean createCacheFiles() {

        if (state == LinkStatus.UNKNOWN) {
            if (!isResponding(true))  // linkcheck hasnt been done, do it now
                return false;
        }

        // ok now uri should be valid, try to use orgFile
        hashGenByContent();
        if (!saveOrigFile()) {
            return setState(LinkStatus.SAVE_ORIG_FAILED);
        }
        if (!generateThumbTiny()) {
            return setState(LinkStatus.GENERATE_THUMB_TINY_FAILED);
        }
        if (!generateThumbBrief()) {
            return setState(LinkStatus.GENERATE_THUMB_BRIEF_FAILED);
        }
        if (!generateThumbFull()) {
            return setState(LinkStatus.GENERATE_THUMB_FULL_FAILED);
        }
        return setState(LinkStatus.CACHE_OK);  // not realy :)
    }



    private boolean generateThumbTiny() {
        /*
        Regarding the image resizing thing
24-05-11 12:10
I remember I used Java2D once
24-05-11 12:10
this is an integral java package
24-05-11 12:11
You can take a look here for an example
24-05-11 12:11
http://tycoontalk.freelancer.com/coding-forum/63227-image-resizing-in-java.html
        */
        return false;
    }

    private boolean generateThumbBrief() {
        return false;
    }

    private boolean generateThumbFull() {
        return false;
    }



    private void hashGenByContent() {

        //r_hash = hashlib.sha256(item).hexdigest().upper()

        hashContent = "123ACE";
    }


    private boolean saveOrigFile() {
        File fileOrg = new File(fileTree.getOriginalFileName(hashContent));

        if (fileOrg.exists()) {
            return true; // same orig already saved
        }

        try {
            FileOutputStream os = new FileOutputStream(fileOrg);
            orgFileConent.writeTo(os);
        } catch (Exception e) {
            System.out.println("failed to save org file");
            if (fileOrg.exists()) {
                fileOrg.delete();
            }
            return false;
        }
        createdFiles.add(fileOrg);
        return true;
    }



    private void initiateParams(FileTree fileTree) {
        this.fileTree = fileTree;
    }


    protected boolean setState(LinkStatus state, String msg){
        boolean b = super.setState(state, msg);
        if ((!b) && (!(createdFiles == null))) {
            // an issue occured remove generated files
            for(File f : createdFiles) {
                f.delete();
            }
        }
        return b;
    }

}