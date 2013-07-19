package eu.europeana.thumbler;

public class LinkTester {
    public static void main(String... args) {
        String msg;

        String[] uris = {"http://www.sunet.se/hjkhjk",
                        "http://www.google.com",
                        "http://www.badlink2.com",
                        };

        // test checking links
        for (String s : uris) {
            CheckUrl cu = new CheckUrl(s);
            if (cu.isResponding()) {
                msg = "ok";
            }
            else {
                msg = cu.getState().toString() + " " + cu.getErrorMessage();
            }
            System.out.println(s + "\t" + msg);
        }
    }
}
