package com.example.committeespinner;

import android.animation.*;
import android.app.*;
import android.os.*;
import android.content.*;
import android.graphics.*;
import android.graphics.drawable.ColorDrawable;
import android.view.*;
import android.view.animation.*;
import android.widget.*;
import java.util.*;

public class MainActivity extends Activity {

    LinearLayout root, pageSpinner, pageMembers, namesBox, historyBox;
    EditText monthsInput, personInput;
    TextView winner;
    WheelView wheel;
    Button spinnerTab, membersTab;

    ArrayList<String> all = new ArrayList<>();
    ArrayList<String> remaining = new ArrayList<>();
    ArrayList<String> history = new ArrayList<>();

    Random random = new Random();
    int selectedMonths = 10;
    boolean spinning = false;

    int dp(float n) {
        return (int)(n * getResources().getDisplayMetrics().density + .5f);
    }

    TextView tv(String s, int size) {
        TextView t = new TextView(this);
        t.setText(s);
        t.setTextSize(size);
        t.setTextColor(Color.rgb(35,35,35));
        t.setPadding(dp(8), dp(8), dp(8), dp(8));
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
        root.setPadding(dp(10), dp(8), dp(10), dp(8));

        LinearLayout nav = new LinearLayout(this);
        nav.setOrientation(LinearLayout.HORIZONTAL);

        spinnerTab = btn("🎡 Spinner");
        membersTab = btn("👥 Members");

        nav.addView(spinnerTab, new LinearLayout.LayoutParams(0, dp(56), 1));
        nav.addView(membersTab, new LinearLayout.LayoutParams(0, dp(56), 1));
        root.addView(nav);

        pageSpinner = new LinearLayout(this);
        pageSpinner.setOrientation(LinearLayout.VERTICAL);
        pageSpinner.setGravity(Gravity.CENTER_HORIZONTAL);

        TextView spinnerTitle = tv("🎡 Committee Lucky Draw", 27);
        spinnerTitle.setGravity(Gravity.CENTER);
        pageSpinner.addView(spinnerTitle);

        wheel = new WheelView(this);
        pageSpinner.addView(wheel, new LinearLayout.LayoutParams(-1, dp(370)));

        winner = tv("Ready", 21);
        winner.setGravity(Gravity.CENTER);
        pageSpinner.addView(winner, new LinearLayout.LayoutParams(-1, dp(62)));

        root.addView(pageSpinner, new LinearLayout.LayoutParams(-1, 0, 1));

        pageMembers = new LinearLayout(this);
        pageMembers.setOrientation(LinearLayout.VERTICAL);

        TextView membersTitle = tv("👥 Committee Members", 25);
        membersTitle.setGravity(Gravity.CENTER);
        pageMembers.addView(membersTitle);

        LinearLayout monthCard = new LinearLayout(this);
        monthCard.setOrientation(LinearLayout.VERTICAL);
        monthCard.setPadding(dp(12), dp(8), dp(12), dp(8));
        monthCard.setBackgroundColor(Color.WHITE);

        TextView monthTitle = tv("Number of months / members", 15);
        monthCard.addView(monthTitle);

        LinearLayout monthRow = new LinearLayout(this);
        monthsInput = new EditText(this);
        monthsInput.setHint("e.g. 12");
        monthsInput.setInputType(2);
        monthsInput.setSingleLine(true);
        monthRow.addView(monthsInput, new LinearLayout.LayoutParams(0, dp(54), 1));

        Button setMonthsButton = btn("Set");
        monthRow.addView(setMonthsButton, new LinearLayout.LayoutParams(dp(85), dp(54)));
        monthCard.addView(monthRow);

        pageMembers.addView(monthCard, new LinearLayout.LayoutParams(-1, dp(130)));

        setMonthsButton.setOnClickListener(v -> setMonths());

        LinearLayout add = new LinearLayout(this);
        personInput = new EditText(this);
        personInput.setHint("Enter member name");
        personInput.setSingleLine(true);
        add.addView(personInput, new LinearLayout.LayoutParams(0, dp(58), 1));

        Button addBtn = btn("Add");
        add.addView(addBtn, new LinearLayout.LayoutParams(dp(90), dp(58)));
        pageMembers.addView(add);

        addBtn.setOnClickListener(v -> addName());

        ScrollView memberScroll = new ScrollView(this);
        namesBox = new LinearLayout(this);
        namesBox.setOrientation(LinearLayout.VERTICAL);
        memberScroll.addView(namesBox);
        pageMembers.addView(memberScroll, new LinearLayout.LayoutParams(-1, 0, 1));

        Button reset = btn("↻  Reset");
        pageMembers.addView(reset, new LinearLayout.LayoutParams(-1, dp(58)));
        reset.setOnClickListener(v -> resetCommittee());

        TextView historyTitle = tv("History", 19);
        pageMembers.addView(historyTitle);

        ScrollView historyScroll = new ScrollView(this);
        historyBox = new LinearLayout(this);
        historyBox.setOrientation(LinearLayout.VERTICAL);
        historyScroll.addView(historyBox);
        pageMembers.addView(historyScroll, new LinearLayout.LayoutParams(-1, dp(150)));

        root.addView(pageMembers, new LinearLayout.LayoutParams(-1, 0, 1));

        spinnerTab.setOnClickListener(v -> showSpinnerPage());
        membersTab.setOnClickListener(v -> showMembersPage());

        setContentView(root);
        showSpinnerPage();
        load();
    }

    void showSpinnerPage() {
        pageSpinner.setVisibility(View.VISIBLE);
        pageMembers.setVisibility(View.GONE);
        spinnerTab.setEnabled(false);
        membersTab.setEnabled(true);
    }

    void showMembersPage() {
        pageSpinner.setVisibility(View.GONE);
        pageMembers.setVisibility(View.VISIBLE);
        spinnerTab.setEnabled(true);
        membersTab.setEnabled(false);
        refresh();
    }

    void setMonths() {
        try {
            int n = Integer.parseInt(monthsInput.getText().toString().trim());
            if (n < 1) {
                Toast.makeText(this, "Enter at least 1 month", Toast.LENGTH_SHORT).show();
                return;
            }

            if (!all.isEmpty()) {
                new AlertDialog.Builder(this)
                        .setTitle("Start new committee?")
                        .setMessage("Changing the number of months will clear the current members and draw.")
                        .setNegativeButton("Cancel", null)
                        .setPositiveButton("Start New", (d, w) -> applyMonths(n))
                        .show();
            } else {
                applyMonths(n);
            }
        } catch (Exception e) {
            Toast.makeText(this, "Enter a valid number", Toast.LENGTH_SHORT).show();
        }
    }

    void applyMonths(int n) {
        selectedMonths = n;
        all.clear();
        remaining.clear();
        history.clear();
        winner.setText("Ready");
        save();
        refresh();
    }

    void addName() {
        String n = personInput.getText().toString().trim();
        if (n.isEmpty()) return;

        if (all.size() >= selectedMonths) {
            Toast.makeText(this, "Maximum " + selectedMonths + " members allowed", Toast.LENGTH_SHORT).show();
            return;
        }

        all.add(n);
        remaining.add(n);
        personInput.setText("");

        wheel.setMembers(remaining);
        refresh();
        save();
    }

    void resetCommittee() {
        remaining = new ArrayList<>(all);
        history.clear();
        winner.setText("Ready");
        historyBox.removeAllViews();
        wheel.stopAnimation();
        wheel.setMembers(remaining);
        spinning = false;
        refresh();
        save();
    }

    void spin() {
        if (spinning) return;

        if (all.size() < selectedMonths) {
            Toast.makeText(this, "Add all " + selectedMonths + " members first", Toast.LENGTH_SHORT).show();
            showMembersPage();
            return;
        }

        // The last remaining person is never spun.
        if (remaining.size() <= 1) {
            if (remaining.size() == 1) showFinalMember(remaining.get(0));
            return;
        }

        spinning = true;
        

        final int selectedIndex = random.nextInt(remaining.size());
        final String selectedName = remaining.get(selectedIndex);

        wheel.spinTo(selectedIndex, () -> {
            // Remove exactly the selected member after the animation finishes.
            remaining.remove(selectedName);

            history.add((history.size() + 1) + ". " + selectedName);
            winner.setText("🏆 " + selectedName);

            historyBox.addView(tv(
                    history.get(history.size() - 1), 18
            ));

            save();
            refresh();

            spinning = false;

            if (remaining.size() <= 1) {
                
            } else {
                
            }

            showWinnerAnimation(selectedName);
        });
    }

    void showFinalMember(String name) {
        winner.setText("🏆 " + name);
        showWinnerAnimation(name);
    }

    void showWinnerAnimation(String name) {
        final Dialog dialog = new Dialog(this);
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);

        LinearLayout box = new LinearLayout(this);
        box.setOrientation(LinearLayout.VERTICAL);
        box.setGravity(Gravity.CENTER);
        box.setPadding(dp(18), dp(18), dp(18), dp(18));
        box.setBackgroundColor(Color.rgb(18, 18, 28));

        TextView title = tv("🎉  WINNER  🎉", 30);
        title.setGravity(Gravity.CENTER);
        title.setTextColor(Color.WHITE);

        TextView nameView = tv(name, 44);
        nameView.setGravity(Gravity.CENTER);
        nameView.setTextColor(Color.WHITE);
        nameView.setPadding(dp(8), dp(35), dp(8), dp(35));

        Button close = btn("CONTINUE");
        close.setTextSize(17);
        close.setOnClickListener(v -> dialog.dismiss());

        box.addView(title, new LinearLayout.LayoutParams(-1, dp(70)));
        box.addView(nameView, new LinearLayout.LayoutParams(-1, dp(150)));
        box.addView(close, new LinearLayout.LayoutParams(-1, dp(58)));

        dialog.setContentView(box);
        dialog.setOnShowListener(x -> {
            Window w = dialog.getWindow();
            if (w != null) {
                w.setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
                w.setLayout(-1, -1);
            }

            title.setAlpha(0f);
            title.animate().alpha(1f).setDuration(450).start();

            nameView.setAlpha(0f);
            nameView.setScaleX(.2f);
            nameView.setScaleY(.2f);
            nameView.animate()
                    .alpha(1f)
                    .scaleX(1f)
                    .scaleY(1f)
                    .setDuration(850)
                    .setInterpolator(new OvershootInterpolator())
                    .start();
        });

        dialog.show();

        Window w = dialog.getWindow();
        if (w != null) {
            w.setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
            w.setLayout(-1, -1);
        }
    }

    void refresh() {
        if (namesBox == null) return;

        if (monthsInput != null) monthsInput.setText(String.valueOf(selectedMonths));

        namesBox.removeAllViews();

        for (int i = 0; i < all.size(); i++) {
            final String originalName = all.get(i);

            LinearLayout row = new LinearLayout(this);
            row.setGravity(Gravity.CENTER_VERTICAL);
            row.setPadding(dp(5), dp(2), dp(5), dp(2));

            // Number instead of a dot.
            TextView number = tv(String.valueOf(i + 1), 18);
            number.setGravity(Gravity.CENTER);
            row.addView(number, new LinearLayout.LayoutParams(dp(38), dp(50)));

            // Editable member name.
            EditText nameEdit = new EditText(this);
            nameEdit.setText(originalName);
            nameEdit.setTextSize(17);
            nameEdit.setSingleLine(true);
            nameEdit.setPadding(dp(6), 0, dp(6), 0);
            row.addView(nameEdit, new LinearLayout.LayoutParams(0, dp(50), 1));

            Button saveName = btn("Save");
            row.addView(saveName, new LinearLayout.LayoutParams(dp(70), dp(50)));

            Button del = btn("Delete");
            row.addView(del, new LinearLayout.LayoutParams(dp(80), dp(50)));

            saveName.setOnClickListener(v -> {
                String newName = nameEdit.getText().toString().trim();

                if (newName.isEmpty()) {
                    Toast.makeText(MainActivity.this,
                            "Name cannot be empty",
                            Toast.LENGTH_SHORT).show();
                    nameEdit.setText(originalName);
                    return;
                }

                int idx = all.indexOf(originalName);
                if (idx >= 0) {
                    all.set(idx, newName);

                    // If this member is still on the wheel, rename it there too.
                    int ridx = remaining.indexOf(originalName);
                    if (ridx >= 0) {
                        remaining.set(ridx, newName);
                    }

                    save();
                    refresh();
                }
            });

            del.setOnClickListener(v -> {
                all.remove(originalName);
                remaining.remove(originalName);
                save();
                refresh();
            });

            namesBox.addView(row);
        }
    }

    void save() {
        getPreferences(0).edit()
                .putInt("months", selectedMonths)
                .putString("all", String.join("\u001F", all))
                .putString("remaining", String.join("\u001F", remaining))
                .putString("history", String.join("\u001F", history))
                .apply();
    }

    void load() {
        android.content.SharedPreferences p = getPreferences(0);

        selectedMonths = p.getInt("months", 10);

        String a = p.getString("all", "");
        String r = p.getString("remaining", "");
        String h = p.getString("history", "");

        if (!a.isEmpty())
            all.addAll(Arrays.asList(a.split("\u001F", -1)));

        if (!r.isEmpty())
            remaining.addAll(Arrays.asList(r.split("\u001F", -1)));

        if (!h.isEmpty()) {
            for (String s : h.split("\u001F", -1)) {
                history.add(s);
                historyBox.addView(tv(s, 18));
            }
        }

        wheel.setMembers(remaining);
        refresh();

    }

    // =========================================================
    // Animated wheel
    // =========================================================
    public class WheelView extends View {

        Paint paint = new Paint(Paint.ANTI_ALIAS_FLAG);
        Paint textPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        ArrayList<String> members = new ArrayList<>();

        float wheelRotation = 0f;
        ValueAnimator activeAnimator;
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
            textPaint.setTypeface(Typeface.DEFAULT_BOLD);
            setLayerType(View.LAYER_TYPE_SOFTWARE, null);
        }

        void setMembers(ArrayList<String> list) {
            members = new ArrayList<>(list);
            if (members.size() == 0) wheelRotation = 0f;
            invalidate();
        }

        void stopAnimation() {
            if (activeAnimator != null) {
                activeAnimator.cancel();
                activeAnimator = null;
            }
            spinFinished = null;
        }

        void spinTo(int index, Runnable done) {
            if (members.size() < 2) {
                if (done != null) done.run();
                return;
            }

            stopAnimation();
            spinFinished = done;

            float slice = 360f / members.size();
            float centerAngle = (index + .5f) * slice;

            // Pointer is at the top (-90 degrees).
            float desired = 360f - centerAngle;
            desired %= 360f;
            if (desired < 0) desired += 360f;

            float currentMod = wheelRotation % 360f;
            if (currentMod < 0) currentMod += 360f;

            float delta = desired - currentMod;
            if (delta < 0) delta += 360f;

            final float start = wheelRotation;
            final float end = wheelRotation + 360f * 8f + delta;

            activeAnimator = ValueAnimator.ofFloat(start, end);
            activeAnimator.setDuration(5200);
            activeAnimator.setInterpolator(new DecelerateInterpolator(2.8f));

            activeAnimator.addUpdateListener(a -> {
                wheelRotation = (Float)a.getAnimatedValue();
                invalidate();
            });

            activeAnimator.addListener(new AnimatorListenerAdapter() {
                boolean called = false;

                void finishOnce() {
                    if (called) return;
                    called = true;

                    activeAnimator = null;
                    wheelRotation = end % 360f;
                    invalidate();

                    if (spinFinished != null) {
                        Runnable r = spinFinished;
                        spinFinished = null;
                        r.run();
                    }
                }

                @Override
                public void onAnimationEnd(Animator animation) {
                    finishOnce();
                }

                @Override
                public void onAnimationCancel(Animator animation) {
                    // A cancelled animation must not select a member.
                    activeAnimator = null;
                    spinFinished = null;
                }
            });

            activeAnimator.start();
        }

        @Override
        protected void onDraw(Canvas canvas) {
            super.onDraw(canvas);

            float cx = getWidth() / 2f;
            float cy = getHeight() / 2f + dp(10);
            float radius = Math.min(getWidth(), getHeight()) * .42f;

            paint.setStyle(Paint.Style.FILL);
            paint.setColor(Color.DKGRAY);
            canvas.drawCircle(cx, cy, radius + dp(8), paint);

            if (members.isEmpty()) {
                paint.setColor(Color.LTGRAY);
                canvas.drawCircle(cx, cy, radius, paint);

                textPaint.setTextAlign(Paint.Align.CENTER);
                textPaint.setTextSize(dp(18));
                textPaint.setColor(Color.DKGRAY);
                canvas.drawText("Add members", cx, cy + dp(6), textPaint);

                drawPointer(canvas, cx, cy, radius);
                return;
            }

            float slice = 360f / members.size();
            RectF rect = new RectF(
                    cx - radius, cy - radius,
                    cx + radius, cy + radius
            );

            canvas.save();
            canvas.rotate(wheelRotation, cx, cy);

            for (int i = 0; i < members.size(); i++) {
                float start = -90f + i * slice;

                paint.setStyle(Paint.Style.FILL);
                paint.setColor(colors[i % colors.length]);
                canvas.drawArc(rect, start, slice, true, paint);

                paint.setStyle(Paint.Style.STROKE);
                paint.setStrokeWidth(dp(2));
                paint.setColor(Color.WHITE);
                canvas.drawArc(rect, start, slice, true, paint);

                drawMemberText(
                        canvas,
                        members.get(i),
                        cx, cy, radius,
                        start + slice / 2f
                );
            }

            canvas.restore();

            paint.setStyle(Paint.Style.FILL);
            paint.setColor(Color.WHITE);
            canvas.drawCircle(cx, cy, dp(34), paint);

            paint.setStyle(Paint.Style.STROKE);
            paint.setStrokeWidth(dp(3));
            paint.setColor(Color.DKGRAY);
            canvas.drawCircle(cx, cy, dp(34), paint);
            paint.setStyle(Paint.Style.FILL);

            textPaint.setTextAlign(Paint.Align.CENTER);
            textPaint.setTextSize(dp(13));
            textPaint.setColor(Color.DKGRAY);
            canvas.drawText("SPIN", cx, cy + dp(5), textPaint);

            drawPointer(canvas, cx, cy, radius);
        }

        void drawMemberText(Canvas canvas, String name,
                            float cx, float cy, float radius,
                            float angle) {

            double rad = Math.toRadians(angle);
            float tx = cx + (float)Math.cos(rad) * radius * .63f;
            float ty = cy + (float)Math.sin(rad) * radius * .63f;

            canvas.save();
            canvas.rotate(angle + 90f, tx, ty);

            textPaint.setColor(Color.WHITE);
            textPaint.setTextSize(
                    members.size() > 18 ? dp(9) :
                    members.size() > 12 ? dp(11) :
                    dp(14)
            );
            textPaint.setTypeface(Typeface.DEFAULT_BOLD);
            textPaint.setTextAlign(Paint.Align.CENTER);

            String shown = name;
            if (shown.length() > 15) {
                shown = shown.substring(0, 14) + "…";
            }

            canvas.drawText(shown, tx, ty, textPaint);
            canvas.restore();
        }

        void drawPointer(Canvas canvas, float cx, float cy, float radius) {
            Path p = new Path();
            float top = cy - radius - dp(2);

            p.moveTo(cx - dp(18), top - dp(3));
            p.lineTo(cx + dp(18), top - dp(3));
            p.lineTo(cx, top + dp(31));
            p.close();

            paint.setStyle(Paint.Style.FILL);
            paint.setColor(Color.BLACK);
            canvas.drawPath(p, paint);

            paint.setStyle(Paint.Style.STROKE);
            paint.setStrokeWidth(dp(2));
            paint.setColor(Color.WHITE);
            canvas.drawPath(p, paint);
            paint.setStyle(Paint.Style.FILL);
        }
    }
}
