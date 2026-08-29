package com.example.committeespinner;

import android.app.*;
import android.os.*;
import android.content.*;
import android.graphics.*;
import android.graphics.drawable.ColorDrawable;
import android.view.*;
import android.view.animation.*;
import android.widget.*;
import java.util.*;

/*
 * Committee Spinner
 *
 * Main changes:
 * 1. Wheel shows ALL remaining members (not a fixed 10).
 * 2. Draws are based on members: Total members - 1.
 * 3. The final remaining member is NOT spun; it is shown as the final member.
 * 4. Real spinning wheel animation.
 * 5. Winner appears in a full-screen animated result screen.
 */
public class MainActivity extends Activity {

    LinearLayout root, namesBox, historyBox;
    EditText monthsInput, personInput;
    TextView drawLabel, winner;
    WheelView wheel;
    Button spinButton;

    ArrayList<String> all = new ArrayList<>();
    ArrayList<String> remaining = new ArrayList<>();
    ArrayList<String> history = new ArrayList<>();

    int months = 10;
    int current = 1;
    boolean spinning = false;

    Random random = new Random();

    int dp(float n) {
        return (int)(n * getResources().getDisplayMetrics().density + .5f);
    }

    TextView tv(String s, int size) {
        TextView t = new TextView(this);
        t.setText(s);
        t.setTextSize(size);
        t.setTextColor(Color.rgb(35,35,35));
        t.setPadding(dp(8),dp(8),dp(8),dp(8));
        return t;
    }

    Button btn(String s) {
        Button b = new Button(this);
        b.setText(s);
        return b;
    }

    @Override
    public void onCreate(Bundle b) {
        super.onCreate(b);
        build();
    }

    void build() {
        root = new LinearLayout(this);
        root.setOrientation(LinearLayout.VERTICAL);
        root.setPadding(dp(14),dp(10),dp(14),dp(10));

        ScrollView sv = new ScrollView(this);
        sv.addView(root);
        setContentView(sv);

        TextView title = tv("🎡 Committee Lucky Draw", 27);
        title.setGravity(Gravity.CENTER);
        root.addView(title);

        TextView sub = tv("Spin the wheel — the last member is kept automatically.", 14);
        sub.setGravity(Gravity.CENTER);
        root.addView(sub);

        // Keep the original months setting, but it no longer limits the number of draws.
        LinearLayout settings = new LinearLayout(this);
        settings.setOrientation(LinearLayout.HORIZONTAL);

        monthsInput = new EditText(this);
        monthsInput.setHint("Months");
        monthsInput.setInputType(2);
        monthsInput.setText("10");
        settings.addView(monthsInput,
                new LinearLayout.LayoutParams(0,dp(56),1));

        Button set = btn("Set Months");
        settings.addView(set,
                new LinearLayout.LayoutParams(0,dp(56),1));
        root.addView(settings);

        set.setOnClickListener(v -> {
            try {
                months = Math.max(1,
                        Integer.parseInt(monthsInput.getText().toString()));
                save();
            } catch (Exception ignored) {}
        });

        drawLabel = tv("Add members to start", 19);
        drawLabel.setGravity(Gravity.CENTER);
        root.addView(drawLabel);

        // REAL wheel
        wheel = new WheelView(this);
        root.addView(wheel,
                new LinearLayout.LayoutParams(-1, dp(340)));

        winner = tv("Ready to spin", 20);
        winner.setGravity(Gravity.CENTER);
        root.addView(winner);

        spinButton = btn("🎯  SPIN THE WHEEL");
        root.addView(spinButton,
                new LinearLayout.LayoutParams(-1,dp(62)));

        spinButton.setOnClickListener(v -> spin());

        root.addView(tv("Members",20));

        LinearLayout add = new LinearLayout(this);
        personInput = new EditText(this);
        personInput.setHint("Enter member name");
        add.addView(personInput,
                new LinearLayout.LayoutParams(0,dp(58),1));

        Button addBtn = btn("Add");
        add.addView(addBtn,
                new LinearLayout.LayoutParams(dp(90),dp(58)));
        root.addView(add);

        addBtn.setOnClickListener(v -> addName());

        namesBox = new LinearLayout(this);
        namesBox.setOrientation(LinearLayout.VERTICAL);
        root.addView(namesBox);

        Button reset = btn("↻  New Committee / Reset");
        root.addView(reset);
        reset.setOnClickListener(v -> {
            remaining = new ArrayList<>(all);
            history.clear();
            current = 1;
            winner.setText("Ready to spin");
            wheel.setMembers(remaining);
            refresh();
            historyBox.removeAllViews();
            updateDrawLabel();
            save();
        });

        root.addView(tv("History",20));
        historyBox = new LinearLayout(this);
        historyBox.setOrientation(LinearLayout.VERTICAL);
        root.addView(historyBox);

        load();
    }

    void addName() {
        String n = personInput.getText().toString().trim();
        if (n.isEmpty()) return;

        all.add(n);
        remaining.add(n);
        personInput.setText("");

        wheel.setMembers(remaining);
        updateDrawLabel();
        save();
        refresh();
    }

    void updateDrawLabel() {
        int total = remaining.size();

        if (total == 0) {
            drawLabel.setText("No members");
        } else if (total == 1) {
            drawLabel.setText("🏆 FINAL MEMBER — no spin needed");
        } else {
            int totalDraws = Math.max(0, all.size() - 1);
            int done = Math.max(0, current - 1);
            drawLabel.setText("Draw " + Math.min(done + 1,totalDraws) +
                    " of " + totalDraws +
                    "  •  " + total + " members on wheel");
        }

        wheel.setMembers(remaining);

        if (remaining.size() <= 1) {
            spinButton.setEnabled(false);
        } else {
            spinButton.setEnabled(!spinning);
        }
    }

    void spin() {
        if (spinning) return;

        // IMPORTANT: always leave exactly one member un-drawn.
        if (remaining.size() <= 1) {
            if (remaining.size() == 1) {
                showFinalMember(remaining.get(0));
            }
            return;
        }

        spinning = true;
        spinButton.setEnabled(false);

        final int selectedIndex = random.nextInt(remaining.size());
        final String selectedName = remaining.get(selectedIndex);

        wheel.spinTo(selectedIndex, () -> {
            remaining.remove(selectedName);

            history.add("Draw " + current + ":  " + selectedName);
            current++;

            winner.setText("🏆 " + selectedName);

            if (historyBox != null) {
                historyBox.addView(tv(
                        history.get(history.size()-1), 18));
            }

            save();
            refresh();
            updateDrawLabel();

            spinning = false;

            // Full-screen animated winner result.
            showWinnerAnimation(selectedName);
        });
    }

    void showWinnerAnimation(String name) {
        final Dialog dialog = new Dialog(this);
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);

        LinearLayout box = new LinearLayout(this);
        box.setOrientation(LinearLayout.VERTICAL);
        box.setGravity(Gravity.CENTER);
        box.setPadding(dp(20),dp(20),dp(20),dp(20));
        box.setBackgroundColor(Color.rgb(18,18,28));

        TextView top = tv("🎉  LUCKY DRAW  🎉", 25);
        top.setGravity(Gravity.CENTER);
        top.setTextColor(Color.WHITE);

        TextView win = tv("WINNER", 30);
        win.setGravity(Gravity.CENTER);
        win.setTextColor(Color.WHITE);

        TextView nameView = tv(name, 42);
        nameView.setGravity(Gravity.CENTER);
        nameView.setTextColor(Color.WHITE);
        nameView.setPadding(dp(10),dp(35),dp(10),dp(35));

        TextView done = tv("✓ Selected successfully", 18);
        done.setGravity(Gravity.CENTER);
        done.setTextColor(Color.WHITE);

        Button close = btn("CONTINUE");
        close.setTextSize(17);
        close.setOnClickListener(v -> dialog.dismiss());

        box.addView(top);
        box.addView(win);
        box.addView(nameView);
        box.addView(done);
        box.addView(close,
                new LinearLayout.LayoutParams(-1,dp(58)));

        dialog.setContentView(box);
        Window w = dialog.getWindow();
        if (w != null) {
            w.setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
            w.setLayout(-1,-1);
        }

        dialog.setOnShowListener(x -> {
            Window ww = dialog.getWindow();
            if (ww != null) {
                ww.setLayout(-1,-1);
            }

            nameView.setScaleX(.15f);
            nameView.setScaleY(.15f);
            nameView.setAlpha(0f);

            nameView.animate()
                    .scaleX(1.0f)
                    .scaleY(1.0f)
                    .alpha(1.0f)
                    .setDuration(850)
                    .setInterpolator(new OvershootInterpolator())
                    .start();

            top.setAlpha(0f);
            top.animate().alpha(1f).setDuration(500).start();

            win.setScaleX(.5f);
            win.setScaleY(.5f);
            win.animate()
                    .scaleX(1f)
                    .scaleY(1f)
                    .setDuration(650)
                    .start();
        });

        dialog.show();

        // show() creates the window; set full-screen again afterwards.
        Window ww = dialog.getWindow();
        if (ww != null) {
            ww.setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
            ww.setLayout(-1,-1);
        }
    }

    void showFinalMember(String name) {
        winner.setText("🏆 FINAL MEMBER: " + name);
        showWinnerAnimation(name);
    }

    void refresh() {
        if (namesBox == null) return;

        namesBox.removeAllViews();

        for (String n : new ArrayList<>(all)) {
            LinearLayout row = new LinearLayout(this);

            TextView x = tv(
                    (remaining.contains(n) ? "• " : "✓ ") + n,
                    18
            );

            row.addView(x,
                    new LinearLayout.LayoutParams(0,dp(48),1));

            Button del = btn("Delete");
            row.addView(del,
                    new LinearLayout.LayoutParams(dp(90),dp(48)));

            del.setOnClickListener(v -> {
                all.remove(n);
                remaining.remove(n);
                wheel.setMembers(remaining);
                updateDrawLabel();
                refresh();
                save();
            });

            namesBox.addView(row);
        }

        updateDrawLabel();
    }

    void save() {
        getPreferences(0).edit()
                .putString("all", String.join("\u001F", all))
                .putString("remaining", String.join("\u001F", remaining))
                .putString("history", String.join("\u001F", history))
                .putInt("months", months)
                .putInt("current", current)
                .apply();
    }

    void load() {
        android.content.SharedPreferences p =
                getPreferences(0);

        months = p.getInt("months",10);
        current = p.getInt("current",1);

        monthsInput.setText("" + months);

        String a = p.getString("all","");
        String r = p.getString("remaining","");
        String h = p.getString("history","");

        if (!a.isEmpty())
            all.addAll(Arrays.asList(a.split("\u001F",-1)));

        if (!r.isEmpty())
            remaining.addAll(Arrays.asList(r.split("\u001F",-1)));

        if (!h.isEmpty()) {
            for (String s : h.split("\u001F",-1)) {
                history.add(s);
                historyBox.addView(tv(s,18));
            }
        }

        wheel.setMembers(remaining);
        refresh();
        updateDrawLabel();
    }

    // =========================================================
    // Custom animated spinning wheel
    // =========================================================
    public class WheelView extends View {

        Paint paint = new Paint(Paint.ANTI_ALIAS_FLAG);
        Paint textPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        ArrayList<String> members = new ArrayList<>();

        float wheelRotation = 0f;
        Runnable spinFinished;

        int[] colors = {
                Color.rgb(239,83,80),
                Color.rgb(66,165,245),
                Color.rgb(102,187,106),
                Color.rgb(255,167,38),
                Color.rgb(171,71,188),
                Color.rgb(38,198,218),
                Color.rgb(255,202,40),
                Color.rgb(236,64,122)
        };

        public WheelView(Context c) {
            super(c);
            paint.setStyle(Paint.Style.FILL);

            textPaint.setColor(Color.WHITE);
            textPaint.setTextSize(dp(15));
            textPaint.setTypeface(Typeface.DEFAULT_BOLD);

            setLayerType(View.LAYER_TYPE_SOFTWARE, null);
        }

        void setMembers(ArrayList<String> list) {
            members = new ArrayList<>(list);
            invalidate();
        }

        void spinTo(int index, Runnable done) {
            if (members.size() < 2) {
                if (done != null) done.run();
                return;
            }

            spinFinished = done;

            float slice = 360f / members.size();

            // Segment center for selected member.
            float centerAngle = (index + .5f) * slice;

            // Put selected segment center at the top pointer.
            float desired = 360f - centerAngle;

            // Normalize target to 0..360.
            desired %= 360f;
            if (desired < 0) desired += 360f;

            float currentMod = wheelRotation % 360f;
            if (currentMod < 0) currentMod += 360f;

            float delta = desired - currentMod;
            if (delta < 0) delta += 360f;

            // Multiple complete rotations for a real spin effect.
            final float start = wheelRotation;
            final float end = wheelRotation + 360f * 6f + delta;

            ValueAnimator animator =
                    ValueAnimator.ofFloat(start,end);

            animator.setDuration(3200);
            animator.setInterpolator(new DecelerateInterpolator(2.2f));

            animator.addUpdateListener(a -> {
                wheelRotation = (Float)a.getAnimatedValue();
                invalidate();
            });

            animator.addListener(new AnimatorListenerAdapter() {
                @Override
                public void onAnimationEnd(Animator animation) {
                    wheelRotation = end % 360f;
                    invalidate();

                    if (spinFinished != null) {
                        Runnable r = spinFinished;
                        spinFinished = null;
                        r.run();
                    }
                }
            });

            animator.start();
        }

        @Override
        protected void onDraw(Canvas canvas) {
            super.onDraw(canvas);

            int w = getWidth();
            int h = getHeight();

            float cx = w / 2f;
            float cy = h / 2f + dp(8);
            float radius = Math.min(w,h) * .42f;

            paint.setStyle(Paint.Style.FILL);

            // Outer shadow/ring.
            paint.setColor(Color.DKGRAY);
            canvas.drawCircle(cx,cy,radius+dp(8),paint);

            if (members.isEmpty()) {
                paint.setColor(Color.LTGRAY);
                canvas.drawCircle(cx,cy,radius,paint);

                textPaint.setColor(Color.DKGRAY);
                textPaint.setTextSize(dp(18));
                textPaint.setTextAlign(Paint.Align.CENTER);
                canvas.drawText("Add members",cx,cy,textPaint);

                drawPointer(canvas,cx,cy,radius);
                return;
            }

            float slice = 360f / members.size();

            canvas.save();
            canvas.rotate(wheelRotation,cx,cy);

            RectF rect = new RectF(
                    cx-radius, cy-radius,
                    cx+radius, cy+radius
            );

            for (int i=0; i<members.size(); i++) {
                paint.setColor(colors[i % colors.length]);

                float start = -90f + i * slice;
                canvas.drawArc(rect,start,slice,true,paint);

                // White separator.
                paint.setStyle(Paint.Style.STROKE);
                paint.setStrokeWidth(dp(2));
                paint.setColor(Color.WHITE);
                canvas.drawArc(rect,start,slice,true,paint);
                paint.setStyle(Paint.Style.FILL);

                drawMemberText(canvas,
                        members.get(i),
                        cx,cy,radius,
                        start + slice/2f);
            }

            canvas.restore();

            // Center circle.
            paint.setColor(Color.WHITE);
            canvas.drawCircle(cx,cy,dp(34),paint);
            paint.setStyle(Paint.Style.STROKE);
            paint.setStrokeWidth(dp(3));
            paint.setColor(Color.DKGRAY);
            canvas.drawCircle(cx,cy,dp(34),paint);
            paint.setStyle(Paint.Style.FILL);

            textPaint.setColor(Color.DKGRAY);
            textPaint.setTextSize(dp(13));
            textPaint.setTextAlign(Paint.Align.CENTER);
            canvas.drawText("SPIN",cx,cy+dp(5),textPaint);

            drawPointer(canvas,cx,cy,radius);
        }

        void drawMemberText(Canvas canvas, String name,
                            float cx, float cy, float radius,
                            float angle) {

            double rad = Math.toRadians(angle);
            float tx = cx + (float)Math.cos(rad) * radius * .63f;
            float ty = cy + (float)Math.sin(rad) * radius * .63f;

            canvas.save();
            canvas.rotate(angle + 90f,tx,ty);

            textPaint.setColor(Color.WHITE);
            textPaint.setTextSize(
                    members.size() > 16 ? dp(10) :
                    members.size() > 11 ? dp(12) :
                    dp(14)
            );
            textPaint.setTypeface(Typeface.DEFAULT_BOLD);
            textPaint.setTextAlign(Paint.Align.CENTER);

            String shown = name;
            if (shown.length() > 14) {
                shown = shown.substring(0,13) + "…";
            }

            canvas.drawText(shown,tx,ty,textPaint);
            canvas.restore();
        }

        void drawPointer(Canvas canvas,float cx,float cy,float radius) {
            Path p = new Path();

            float top = cy-radius-dp(2);

            p.moveTo(cx-dp(17),top-dp(3));
            p.lineTo(cx+dp(17),top-dp(3));
            p.lineTo(cx,top+dp(30));
            p.close();

            paint.setColor(Color.BLACK);
            canvas.drawPath(p,paint);

            paint.setStyle(Paint.Style.STROKE);
            paint.setStrokeWidth(dp(2));
            paint.setColor(Color.WHITE);
            canvas.drawPath(p,paint);
            paint.setStyle(Paint.Style.FILL);
        }
    }
}
