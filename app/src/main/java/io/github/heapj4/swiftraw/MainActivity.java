package io.github.heapj4.swiftraw;

import android.app.*;
import android.content.*;
import android.graphics.*;
import android.graphics.drawable.ColorDrawable;
import android.net.Uri;
import android.os.*;
import android.provider.DocumentsContract;
import android.view.*;
import android.widget.*;

import java.io.*;
import java.security.MessageDigest;
import java.util.*;
import java.util.concurrent.*;

public class MainActivity extends Activity {
    private static final int PICK_TREE = 41;
    private static final int BG = Color.rgb(16,16,20), CARD = Color.rgb(31,31,38), TEXT = Color.WHITE;
    private static final Set<String> SUPPORTED = new HashSet<>(Arrays.asList(
            "nef","nrw","cr2","cr3","arw","sr2","srf","raf","orf","rw2","raw","pef","dng","rwl",
            "3fr","fff","iiq","x3f","srw","dcr","kdc","mrw","erf","mos","mef","bay","cap",
            "jpg","jpeg","png","webp","heic","heif","tif","tiff","bmp"));
    private final ArrayList<Photo> photos = new ArrayList<>();
    private final ExecutorService workers = Executors.newFixedThreadPool(3);
    private GridView grid;
    private TextView status;
    private GalleryAdapter adapter;
    private File thumbDir, previewDir;

    @Override public void onCreate(Bundle state) {
        super.onCreate(state);
        getWindow().setStatusBarColor(BG); getWindow().setNavigationBarColor(BG);
        thumbDir = new File(getCacheDir(), "raw_thumbnails"); previewDir = new File(getCacheDir(), "raw_previews");
        thumbDir.mkdirs(); previewDir.mkdirs(); buildUi();
        String saved = getPreferences(MODE_PRIVATE).getString("tree", null);
        if (saved != null) scan(Uri.parse(saved));
    }

    private void buildUi() {
        LinearLayout root = new LinearLayout(this); root.setOrientation(LinearLayout.VERTICAL); root.setPadding(dp(14),dp(12),dp(14),dp(8)); root.setBackgroundColor(BG);
        TextView title = text(getString(R.string.app_name), 26, TEXT); title.setTypeface(null, android.graphics.Typeface.BOLD); root.addView(title);
        TextView sub = text(getString(R.string.tagline), 13, 0xffaaaab5); root.addView(sub);
        LinearLayout actions = new LinearLayout(this); actions.setPadding(0,dp(10),0,dp(8));
        Button open = button(getString(R.string.open_sd_card)); open.setOnClickListener(v -> choose()); actions.addView(open, new LinearLayout.LayoutParams(0,dp(48),1));
        Button export = button(getString(R.string.export_jpgs)); export.setOnClickListener(v -> exportAll()); LinearLayout.LayoutParams ep = new LinearLayout.LayoutParams(0,dp(48),1); ep.leftMargin=dp(8); actions.addView(export,ep);
        root.addView(actions);
        status = text(getString(R.string.choose_folder), 14, 0xffc6c6d0); status.setPadding(0,0,0,dp(8)); root.addView(status);
        grid = new GridView(this); grid.setNumColumns(3); grid.setHorizontalSpacing(dp(5)); grid.setVerticalSpacing(dp(5)); grid.setStretchMode(GridView.STRETCH_COLUMN_WIDTH); grid.setSelector(new ColorDrawable(Color.TRANSPARENT));
        adapter = new GalleryAdapter(); grid.setAdapter(adapter); grid.setOnItemClickListener((p,v,i,id)->openPhoto(photos.get(i))); root.addView(grid,new LinearLayout.LayoutParams(-1,0,1));
        setContentView(root);
    }

    private void choose() {
        Intent i = new Intent(Intent.ACTION_OPEN_DOCUMENT_TREE); i.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION|Intent.FLAG_GRANT_PERSISTABLE_URI_PERMISSION); startActivityForResult(i,PICK_TREE);
    }

    @Override protected void onActivityResult(int req,int result,Intent data) {
        super.onActivityResult(req,result,data); if(req!=PICK_TREE||result!=RESULT_OK||data==null)return;
        Uri tree=data.getData(); if(tree==null)return;
        try { getContentResolver().takePersistableUriPermission(tree, Intent.FLAG_GRANT_READ_URI_PERMISSION); } catch(Exception ignored) {}
        getPreferences(MODE_PRIVATE).edit().putString("tree",tree.toString()).apply(); scan(tree);
    }

    private void scan(Uri tree) {
        photos.clear(); adapter.notifyDataSetChanged(); status.setText(R.string.searching);
        workers.execute(() -> {
            ArrayList<Photo> found=new ArrayList<>();
            try { walk(DocumentsContract.buildDocumentUriUsingTree(tree,DocumentsContract.getTreeDocumentId(tree)),found); } catch(Exception ignored) {}
            Collections.sort(found, (a,b)->Long.compare(b.modified,a.modified));
            runOnUiThread(() -> { photos.addAll(found); adapter.notifyDataSetChanged(); status.setText(found.isEmpty()?getString(R.string.no_supported_photos):getResources().getQuantityString(R.plurals.photos_found,found.size(),found.size())); });
        });
    }

    private void walk(Uri dir, ArrayList<Photo> out) {
        Uri children=DocumentsContract.buildChildDocumentsUriUsingTree(dir,DocumentsContract.getDocumentId(dir));
        String[] cols={DocumentsContract.Document.COLUMN_DOCUMENT_ID,DocumentsContract.Document.COLUMN_DISPLAY_NAME,DocumentsContract.Document.COLUMN_MIME_TYPE,DocumentsContract.Document.COLUMN_SIZE,DocumentsContract.Document.COLUMN_LAST_MODIFIED};
        try(android.database.Cursor c=getContentResolver().query(children,cols,null,null,null)) {
            if(c==null)return; while(c.moveToNext()) {
                String id=c.getString(0),name=c.getString(1),mime=c.getString(2); Uri u=DocumentsContract.buildDocumentUriUsingTree(dir,id);
                if(DocumentsContract.Document.MIME_TYPE_DIR.equals(mime)) walk(u,out);
                else if(supported(name)) out.add(new Photo(u,name,c.isNull(3)?0:c.getLong(3),c.isNull(4)?0:c.getLong(4)));
            }
        } catch(Exception ignored) {}
    }

    private boolean supported(String name) { int p=name.lastIndexOf('.'); return p>0&&SUPPORTED.contains(name.substring(p+1).toLowerCase(Locale.ROOT)); }
    private boolean normal(String n) { String x=n.toLowerCase(Locale.ROOT); return x.matches(".*\\.(jpe?g|png|webp|heic|heif|tiff?|bmp)$"); }

    private Bitmap thumbnail(Photo p,int target) {
        File f=new File(thumbDir,key(p)+".jpg"); Bitmap cached=BitmapFactory.decodeFile(f.getPath()); if(cached!=null)return cached;
        Bitmap b=null;
        try {
            if(normal(p.name)) try(InputStream in=getContentResolver().openInputStream(p.uri)){ b=BitmapFactory.decodeStream(in); }
            else { byte[] jpg=rawJpeg(p.uri); if(jpg!=null)b=BitmapFactory.decodeByteArray(jpg,0,jpg.length); }
            if(b!=null) { float s=Math.min(1f,target/(float)Math.min(b.getWidth(),b.getHeight())); Bitmap small=Bitmap.createScaledBitmap(b,Math.max(1,(int)(b.getWidth()*s)),Math.max(1,(int)(b.getHeight()*s)),true); try(FileOutputStream o=new FileOutputStream(f)){small.compress(Bitmap.CompressFormat.JPEG,82,o);} if(small!=b)b.recycle(); b=small; }
        } catch(Exception ignored) {}
        return b;
    }

    private byte[] rawJpeg(Uri uri) throws IOException {
        byte[] data=readAll(uri); int bestStart=-1,bestEnd=-1,start=-1;
        for(int i=0;i<data.length-1;i++) {
            int a=data[i]&255,b=data[i+1]&255;
            if(a==0xff&&b==0xd8) start=i;
            else if(start>=0&&a==0xff&&b==0xd9) { int end=i+2; if(bestStart<0||end-start>bestEnd-bestStart){bestStart=start;bestEnd=end;} start=-1; }
        }
        return bestStart<0?null:Arrays.copyOfRange(data,bestStart,bestEnd);
    }

    private byte[] readAll(Uri uri) throws IOException {
        try(InputStream in=getContentResolver().openInputStream(uri); ByteArrayOutputStream out=new ByteArrayOutputStream()) { if(in==null)throw new IOException(); byte[] buf=new byte[65536]; int n; while((n=in.read(buf))>0)out.write(buf,0,n); return out.toByteArray(); }
    }

    private void openPhoto(Photo p) {
        Dialog d=new Dialog(this,android.R.style.Theme_Black_NoTitleBar_Fullscreen); FrameLayout frame=new FrameLayout(this); frame.setBackgroundColor(Color.BLACK);
        ZoomImage image=new ZoomImage(this); image.setScaleType(ImageView.ScaleType.FIT_CENTER); frame.addView(image,new FrameLayout.LayoutParams(-1,-1));
        TextView info=text(getString(R.string.loading_preview,p.name,human(p.size)),14,Color.WHITE); info.setBackgroundColor(0xaa000000); info.setPadding(dp(12),dp(8),dp(12),dp(8)); FrameLayout.LayoutParams ip=new FrameLayout.LayoutParams(-1,-2,Gravity.BOTTOM); frame.addView(info,ip);
        Button close=button(getString(R.string.close)); FrameLayout.LayoutParams cp=new FrameLayout.LayoutParams(dp(52),dp(52),Gravity.TOP|Gravity.END); cp.setMargins(0,dp(12),dp(12),0); frame.addView(close,cp); close.setOnClickListener(v->d.dismiss()); d.setContentView(frame); d.show();
        workers.execute(() -> { Bitmap b=thumbnail(p,2200); runOnUiThread(() -> { if(b!=null){image.setImageBitmap(b);info.setText(getString(R.string.preview_loaded,p.name,human(p.size)));}else info.setText(getString(R.string.no_embedded_preview,p.name)); }); });
    }

    private void exportAll() {
        if(photos.isEmpty()){toast(getString(R.string.no_photos_to_export));return;} status.setText(R.string.exporting);
        workers.execute(() -> { File dir=new File(getExternalFilesDir(null),"Converted"); dir.mkdirs(); int count=0;
            for(Photo p:photos) try { byte[] bytes=normal(p.name)?readAll(p.uri):rawJpeg(p.uri); if(bytes==null)continue; String name=p.name.replaceFirst("(?i)\\.[^.]+$","")+".jpg"; try(FileOutputStream o=new FileOutputStream(new File(dir,name))){o.write(bytes);} count++; } catch(Exception ignored){}
            int done=count; runOnUiThread(()->{status.setText(getString(R.string.exported,done));toast(getString(R.string.export_complete));}); });
    }

    private String key(Photo p){try{MessageDigest d=MessageDigest.getInstance("SHA-1");byte[] h=d.digest((p.uri+":"+p.modified+":"+p.size).getBytes("UTF-8"));StringBuilder s=new StringBuilder();for(byte b:h)s.append(String.format("%02x",b));return s.toString();}catch(Exception e){return String.valueOf(p.uri.hashCode());}}
    private String human(long b){return b<1048576?String.format(Locale.US,"%.0f KB",b/1024f):String.format(Locale.US,"%.1f MB",b/1048576f);}
    private TextView text(String s,int sp,int color){TextView v=new TextView(this);v.setText(s);v.setTextSize(sp);v.setTextColor(color);return v;}
    private Button button(String s){Button b=new Button(this);b.setText(s);b.setTextColor(TEXT);b.setTextSize(12);b.setBackgroundColor(0xff5d43da);return b;}
    private int dp(int v){return (int)(v*getResources().getDisplayMetrics().density+.5f);}
    private void toast(String s){Toast.makeText(this,s,Toast.LENGTH_SHORT).show();}

    static class Photo { final Uri uri; final String name; final long size,modified; Photo(Uri u,String n,long s,long m){uri=u;name=n;size=s;modified=m;} }

    class GalleryAdapter extends BaseAdapter {
        public int getCount(){return photos.size();} public Object getItem(int i){return photos.get(i);} public long getItemId(int i){return i;}
        public View getView(int pos,View old,android.view.ViewGroup parent){ImageView v=old instanceof ImageView?(ImageView)old:new ImageView(MainActivity.this);v.setLayoutParams(new GridView.LayoutParams(-1,dp(126)));v.setScaleType(ImageView.ScaleType.CENTER_CROP);v.setBackgroundColor(CARD);v.setImageDrawable(null);Photo p=photos.get(pos);v.setTag(p.uri);
            workers.execute(()->{Bitmap b=thumbnail(p,420);runOnUiThread(()->{if(p.uri.equals(v.getTag())&&b!=null)v.setImageBitmap(b);});});return v;}
    }

    static class ZoomImage extends ImageView {
        private final Matrix matrix=new Matrix(); private final ScaleGestureDetector scale; private float x,y;
        ZoomImage(Context c){super(c);setScaleType(ScaleType.MATRIX);scale=new ScaleGestureDetector(c,new ScaleGestureDetector.SimpleOnScaleGestureListener(){public boolean onScale(ScaleGestureDetector d){matrix.postScale(d.getScaleFactor(),d.getScaleFactor(),d.getFocusX(),d.getFocusY());setImageMatrix(matrix);return true;}});}
        @Override public boolean performClick(){super.performClick();return true;}
        @Override public boolean onTouchEvent(android.view.MotionEvent e){scale.onTouchEvent(e);if(e.getPointerCount()==1){if(e.getAction()==0){x=e.getX();y=e.getY();}else if(e.getAction()==2){matrix.postTranslate(e.getX()-x,e.getY()-y);x=e.getX();y=e.getY();setImageMatrix(matrix);}else if(e.getAction()==1){performClick();}}return true;}
    }
}
