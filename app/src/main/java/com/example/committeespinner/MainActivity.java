package com.example.committeespinner;

import android.animation.*;
import android.app.*;
import android.content.*;
import android.os.Bundle;
import android.graphics.*;
import android.graphics.drawable.ColorDrawable;
import android.view.*;
import android.view.animation.*;
import android.widget.*;
import java.util.*;

public class MainActivity extends Activity {

    LinearLayout root, content, monthsPage, membersPage, wheelPage, historyPage;
    LinearLayout namesBox, historyBox;
    TextView monthValue, memberCount, wheelStatus, winnerText, historyCount;
    EditText personInput;
    WheelView wheel;

    Button monthsTab, membersTab, wheelTab, historyTab;

    ArrayList<String> all = new ArrayList<>();
    ArrayList<String> remaining = new ArrayList<>();
    ArrayList<String> history = new ArrayList<>();

    int selectedMonths = 12;
    boolean spinning = false;
    Random random = new Random();

    int dp(float n) {
        return (int)(n * getResources().getDisplayMetrics().density + 0.5f);
    }

    TextView label(String s, float size) {
        TextView t = new TextView(this);
        t.setText(s);
        t.setTextSize(size);
        t.setTextColor(Color.rgb(35, 38, 48));
        t.setPadding(dp(8), dp(7), dp(8), dp(7));
        return t;
    }

    Button navButton(String s) {
        Button b = new Button(this);
        b.setText(s);
        b.setTextSize(13);
        b.setAllCaps(false);
        return b;
    }

    Button actionButton(String s) {
        Button b = new Button(this);
        b.setText(s);
        b.setTextSize(16);
        b.setAllCaps(false);
        return b;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        load();
        build();
        refreshAll();
        showPage("months");
    }

    void build() {
        root = new LinearLayout(this);
        root.setOrientation(LinearLayout.VERTICAL);
        root.setPadding(dp(8), dp(8), dp(8), dp(8));
        root.setBackgroundColor(Color.rgb(247, 248, 252));

        content = new LinearLayout(this);
        content.setOrientation(LinearLayout.VERTICAL);
        root.addView(content, new LinearLayout.LayoutParams(-1, 0, 1));

        buildMonthsPage();
        buildMembersPage();
        buildWheelPage();
        buildHistoryPage();

        LinearLayout nav = new LinearLayout(this);
        nav.setOrientation(LinearLayout.HORIZONTAL);
        nav.setPadding(0, dp(6), 0, 0);

        monthsTab = navButton("📅\nMonths");
        membersTab = navButton("👥\nMembers");
        wheelTab = navButton("🎡\nWheel");
        historyTab = navButton("📜\nHistory");

        nav.addView(monthsTab, new LinearLayout.LayoutParams(0, dp(62), 1));
        nav.addView(membersTab, new LinearLayout.LayoutParams(0, dp(62), 1));
        nav.addView(wheelTab, new LinearLayout.LayoutParams(0, dp(62), 1));
        nav.addView(historyTab, new LinearLayout.LayoutParams(0, dp(62), 1));

        root.addView(nav);

        monthsTab.setOnClickListener(v -> showPage("months"));
        membersTab.setOnClickListener(v -> showPage("members"));
        wheelTab.setOnClickListener(v -> showPage("wheel"));
        historyTab.setOnClickListener(v -> showPage("history"));

        setContentView(root);
    }

    void buildMonthsPage() {
        monthsPage = page();
        monthsPage.setGravity(Gravity.CENTER_HORIZONTAL);

        TextView title = label("📅  Select Months", 29);
        title.setGravity(Gravity.CENTER);
        monthsPage.addView(title, new LinearLayout.LayoutParams(-1, dp(70)));

        TextView sub = label("Choose how many months are in the committee draw.", 15);
        sub.setGravity(Gravity.CENTER);
        monthsPage.addView(sub);

        LinearLayout card = new LinearLayout(this);
        card.setOrientation(LinearLayout.VERTICAL);
        card.setGravity(Gravity.CENTER);
        card.setPadding(dp(20), dp(20), dp(20), dp(20));
        card.setBackgroundColor(Color.WHITE);

        TextView selected = label("Selected months", 17);
        selected.setGravity(Gravity.CENTER);
        card.addView(selected);

        monthValue = label(String.valueOf(selectedMonths), 54);
        monthValue.setGravity(Gravity.CENTER);
        monthValue.setTextColor(Color.rgb(55, 75, 180));
        card.addView(monthValue, new LinearLayout.LayoutParams(-1, dp(80)));

        LinearLayout controls = new LinearLayout(this);
        controls.setGravity(Gravity.CENTER);

        Button minus = actionButton("−");
        Button plus = actionButton("+");
        controls.addView(minus, new LinearLayout.LayoutParams(dp(90), dp(60)));
        controls.addView(plus, new LinearLayout.LayoutParams(dp(90), dp(60)));
        card.addView(controls);

        monthsPage.addView(card, new LinearLayout.LayoutParams(-1, dp(260)));

        memberCount = label("", 17);
        memberCount.setGravity(Gravity.CENTER);
        monthsPage.addView(memberCount, new LinearLayout.LayoutParams(-1, dp(60)));

        Button next = actionButton("Continue to Members  →");
        monthsPage.addView(next, new LinearLayout.LayoutParams(-1, dp(62)));

        minus.setOnClickListener(v -> changeMonths(-1));
        plus.setOnClickListener(v -> changeMonths(1));
        next.setOnClickListener(v -> showPage("members"));
    }

    void changeMonths(int amount) {
        int n = selectedMonths + amount;
        if (n < 1) n = 1;
        if (n > 60) n = 60;

        if (!all.isEmpty() && n < all.size()) {
            toast("First remove members above " + n);
            return;
        }

        selectedMonths = n;
        monthValue.setText(String.valueOf(selectedMonths));
        save();
        refreshAll();
    }

    void buildMembersPage() {
        membersPage = page();

        TextView title = label("👥  Add Members", 28);
        title.setGravity(Gravity.CENTER);
        membersPage.addView(title, new LinearLayout.LayoutParams(-1, dp(62)));

        memberCount = label("", 16);
        memberCount.setGravity(Gravity.CENTER);
        membersPage.addView(memberCount);

        LinearLayout addRow = new LinearLayout(this);
        personInput = new EditText(this);
        personInput.setHint("Member name");
        personInput.setSingleLine(true);
        addRow.addView(personInput, new LinearLayout.LayoutParams(0, dp(58), 1));

        Button add = actionButton("Add");
        addRow.addView(add, new LinearLayout.LayoutParams(dp(88), dp(58)));
        membersPage.addView(addRow);

        add.setOnClickListener(v -> addName());

        ScrollView scroll = new ScrollView(this);
        namesBox = new LinearLayout(this);
        namesBox.setOrientation(LinearLayout.VERTICAL);
        scroll.addView(namesBox);
        membersPage.addView(scroll, new LinearLayout.LayoutParams(-1, 0, 1));

        Button next = actionButton("Continue to Wheel  →");
        membersPage.addView(next, new LinearLayout.LayoutParams(-1, dp(60)));
        next.setOnClickListener(v -> {
            if (all.size() != selectedMonths) {
                toast("Add all " + selectedMonths + " members first");
                return;
            }
            showPage("wheel");
        });
    }

    void buildWheelPage() {
        wheelPage = page();
        wheelPage.setGravity(Gravity.CENTER_HORIZONTAL);

        TextView title = label("🎡  Lucky Draw", 28);
        title.setGravity(Gravity.CENTER);
        wheelPage.addView(title, new LinearLayout.LayoutParams(-1, dp(62)));

        wheelStatus = label("", 15);
        wheelStatus.setGravity(Gravity.CENTER);
        wheelPage.addView(wheelStatus, new LinearLayout.LayoutParams(-1, dp(40)));

        wheel = new WheelView(this);
        wheelPage.addView(wheel, new LinearLayout.LayoutParams(-1, 0, 1));

        winnerText = label("Tap SPIN", 22);
        winnerText.setGravity(Gravity.CENTER);
        wheelPage.addView(winnerText, new LinearLayout.LayoutParams(-1, dp(70)));

        TextView hint = label("Tap the SPIN button in the centre of the wheel", 13);
        hint.setGravity(Gravity.CENTER);
        wheelPage.addView(hint, new LinearLayout.LayoutParams(-1, dp(35)));
    }

    void buildHistoryPage() {
        historyPage = page();

        TextView title = label("📜  Draw History", 28);
        title.setGravity(Gravity.CENTER);
        historyPage.addView(title, new LinearLayout.LayoutParams(-1, dp(62)));

        historyCount = label("", 15);
        historyCount.setGravity(Gravity.CENTER);
        historyPage.addView(historyCount);

        ScrollView scroll = new ScrollView(this);
        historyBox = new LinearLayout(this);
        historyBox.setOrientation(LinearLayout.VERTICAL);
        scroll.addView(historyBox);
        historyPage.addView(scroll, new LinearLayout.LayoutParams(-1, 0, 1));

        Button reset = actionButton("↻  Reset Draw");
        historyPage.addView(reset, new LinearLayout.LayoutParams(-1, dp(60)));
        reset.setOnClickListener(v -> resetDraw());
    }

    LinearLayout page() {
        LinearLayout p = new LinearLayout(this);
        p.setOrientation(LinearLayout.VERTICAL);
        p.setPadding(dp(5), dp(5), dp(5), dp(5));
        return p;
    }

    void showPage(String page) {
        monthsPage.setVisibility(View.GONE);
        membersPage.setVisibility(View.GONE);
        wheelPage.setVisibility(View.GONE);
        historyPage.setVisibility(View.GONE);

        if (page.equals("months")) monthsPage.setVisibility(View.VISIBLE);
        if (page.equals("members")) membersPage.setVisibility(View.VISIBLE);
        if (page.equals("wheel")) wheelPage.setVisibility(View.VISIBLE);
        if (page.equals("history")) historyPage.setVisibility(View.VISIBLE);

        refreshAll();
    }

    void addName() {
        String name = personInput.getText().toString().trim();

        if (name.isEmpty()) return;

        if (all.size() >= selectedMonths) {
            toast("Maximum " + selectedMonths + " members allowed");
            return;
        }

        all.add(name);
        remaining.add(name);
        personInput.setText("");
        save();
        refreshAll();
    }

    void renameMember(String oldName, String newName) {
        newName = newName.trim();

        if (newName.isEmpty()) {
            toast("Name cannot be empty");
            return;
        }

        int i = all.indexOf(oldName);
        if (i >= 0) all.set(i, newName);

        int r = remaining.indexOf(oldName);
        if (r >= 0) remaining.set(r, newName);

        save();
        refreshAll();
    }

    void deleteMember(String name) {
        all.remove(name);
        remaining.remove(name);
        save();
        refreshAll();
    }

    void refreshAll() {
        if (monthValue != null) monthValue.setText(String.valueOf(selectedMonths));

        if (memberCount != null) {
            memberCount.setText("Members added: " + all.size() + " / " + selectedMonths);
        }

        if (wheelStatus != null) {
            if (all.size() < selectedMonths) {
                wheelStatus.setText("Add " + (selectedMonths - all.size()) + " more members");
            } else if (remaining.size() <= 1) {
                wheelStatus.setText("Final member remains");
            } else {
                wheelStatus.setText(remaining.size() + " members remaining");
            }
        }

        if (historyCount != null) {
            historyCount.setText("Draws completed: " + history.size());
        }

        if (wheel != null) wheel.setMembers(remaining);

        refreshMembers();
        refreshHistory();
    }

    void refreshMembers() {
        if (namesBox == null) return;

        namesBox.removeAllViews();

        for (int i = 0; i < all.size(); i++) {
            final int memberIndex = i;
            final String original = all.get(i);

            LinearLayout row = new LinearLayout(this);
            row.setGravity(Gravity.CENTER_VERTICAL);

            TextView number = label(String.valueOf(i + 1), 17);
            number.setGravity(Gravity.CENTER);
            row.addView(number, new LinearLayout.LayoutParams(dp(42), dp(56)));

            EditText name = new EditText(this);
            name.setText(original);
            name.setTextSize(17);
            name.setSingleLine(true);
            name.setTag(original);
            row.addView(name, new LinearLayout.LayoutParams(0, dp(56), 1));

            Button saveName = navButton("Save");
            saveName.setVisibility(View.GONE);
            row.addView(saveName, new LinearLayout.LayoutParams(dp(68), dp(56)));

            TextView saved = label("Saved", 14);
            saved.setGravity(Gravity.CENTER);
            saved.setTextColor(Color.rgb(40, 140, 75));
            saved.setVisibility(View.GONE);
            row.addView(saved, new LinearLayout.LayoutParams(dp(68), dp(56)));

            Button del = navButton("Delete");
            row.addView(del, new LinearLayout.LayoutParams(dp(78), dp(56)));

            name.addTextChangedListener(new android.text.TextWatcher() {
                @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
                @Override public void onTextChanged(CharSequence s, int start, int before, int count) {
                    String current = s.toString().trim();
                    String baseline = String.valueOf(name.getTag());
                    boolean changed = !current.equals(baseline);
                    saveName.setVisibility(changed ? View.VISIBLE : View.GONE);
                    if (changed) saved.setVisibility(View.GONE);
                }
                @Override public void afterTextChanged(android.text.Editable s) {}
            });

            saveName.setOnClickListener(v -> {
                String newName = name.getText().toString().trim();
                String oldName = String.valueOf(name.getTag());

                if (newName.isEmpty()) {
                    toast("Name cannot be empty");
                    name.setText(oldName);
                    return;
                }

                for (int j = 0; j < all.size(); j++) {
                    if (j != memberIndex && all.get(j).equalsIgnoreCase(newName)) {
                        toast("This name already exists");
                        name.setText(oldName);
                        return;
                    }
                }

                if (memberIndex < all.size()) all.set(memberIndex, newName);
                int r = remaining.indexOf(oldName);
                if (r >= 0) remaining.set(r, newName);

                name.setTag(newName);
                saveName.setVisibility(View.GONE);
                saved.setVisibility(View.VISIBLE);
                save();
                if (wheel != null) wheel.setMembers(remaining);
            });

            del.setOnClickListener(v -> deleteMember(original));
            namesBox.addView(row);
        }
    }

    void refreshHistory() {
        if (historyBox == null) return;

        historyBox.removeAllViews();

        if (history.isEmpty()) {
            TextView empty = label("No draws yet.", 17);
            empty.setGravity(Gravity.CENTER);
            historyBox.addView(empty);
            return;
        }

        for (String item : history) {
            TextView h = label(item, 18);
            historyBox.addView(h);
        }
    }

    void spin() {
        if (spinning) return;

        if (all.size() != selectedMonths) {
            toast("Add all " + selectedMonths + " members first");
            return;
        }

        if (remaining.size() <= 1) {
            if (remaining.size() == 1) showWinner(remaining.get(0));
            return;
        }

        spinning = true;

        int index = random.nextInt(remaining.size());
        String chosen = remaining.get(index);

        wheel.spinTo(index, () -> {
            remaining.remove(chosen);

            history.add((history.size() + 1) + ".  " + chosen);

            winnerText.setText("🏆  " + chosen);
            spinning = false;

            save();
            refreshAll();
            showWinner(chosen);
        });
    }

    void showWinner(String name) {
        final Dialog d = new Dialog(this);
        d.requestWindowFeature(Window.FEATURE_NO_TITLE);

        LinearLayout box = new LinearLayout(this);
        box.setOrientation(LinearLayout.VERTICAL);
        box.setGravity(Gravity.CENTER);
        box.setPadding(dp(25), dp(25), dp(25), dp(25));
        box.setBackgroundColor(Color.rgb(20, 22, 32));

        TextView star = label("✨", 58);
        star.setGravity(Gravity.CENTER);

        TextView title = label("WINNER", 32);
        title.setTextColor(Color.WHITE);
        title.setGravity(Gravity.CENTER);

        TextView n = label(name, 42);
        n.setTextColor(Color.WHITE);
        n.setGravity(Gravity.CENTER);
        n.setPadding(0, dp(25), 0, dp(30));

        Button ok = actionButton("Continue");
        ok.setOnClickListener(v -> d.dismiss());

        box.addView(star);
        box.addView(title);
        box.addView(n);
        box.addView(ok, new LinearLayout.LayoutParams(-1, dp(58)));

        d.setContentView(box);
        d.show();

        Window w = d.getWindow();
        if (w != null) {
            w.setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
            w.setLayout(-1, -1);
        }

        n.setAlpha(0f);
        n.setScaleX(.2f);
        n.setScaleY(.2f);
        n.animate()
                .alpha(1f)
                .scaleX(1f)
                .scaleY(1f)
                .setDuration(750)
                .setInterpolator(new OvershootInterpolator())
                .start();
    }

    void resetDraw() {
        remaining = new ArrayList<>(all);
        history.clear();
        spinning = false;
        winnerText.setText("Tap SPIN");
        wheel.stopAnimation();
        save();
        refreshAll();
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

        selectedMonths = p.getInt("months", 12);

        String a = p.getString("all", "");
        String r = p.getString("remaining", "");
        String h = p.getString("history", "");

        if (!a.isEmpty()) all.addAll(Arrays.asList(a.split("\u001F", -1)));
        if (!r.isEmpty()) remaining.addAll(Arrays.asList(r.split("\u001F", -1)));
        if (!h.isEmpty()) history.addAll(Arrays.asList(h.split("\u001F", -1)));

        if (remaining.isEmpty() && !all.isEmpty()) {
            remaining = new ArrayList<>(all);
        }
    }

    void toast(String s) {
        Toast.makeText(this, s, Toast.LENGTH_SHORT).show();
    }

    // ============================================================
    // Animated wheel
    // ============================================================
    public class WheelView extends View {

        Paint paint = new Paint(Paint.ANTI_ALIAS_FLAG);
        Paint textPaint = new Paint(Paint.ANTI_ALIAS_FLAG);

        ArrayList<String> members = new ArrayList<>();
        ValueAnimator animator;
        Runnable finishAction;
        float rotation = 0f;

        int[] colors = {
                Color.rgb(76, 60, 190),
                Color.rgb(31, 125, 205),
                Color.rgb(0, 145, 125),
                Color.rgb(235, 120, 15),
                Color.rgb(190, 45, 55),
                Color.rgb(120, 45, 155),
                Color.rgb(25, 105, 175),
                Color.rgb(50, 130, 65),
                Color.rgb(220, 85, 15),
                Color.rgb(165, 25, 90)
        };

        WheelView(Context c) {
            super(c);
            setLayerType(View.LAYER_TYPE_SOFTWARE, null);
            setClickable(true);
            textPaint.setTypeface(Typeface.DEFAULT_BOLD);
        }

        void setMembers(ArrayList<String> list) {
            members = new ArrayList<>(list);
            invalidate();
        }

        boolean canSpin() {
            return !spinning && all.size() == selectedMonths && members.size() > 1;
        }

        @Override
        public boolean onTouchEvent(android.view.MotionEvent e) {
            if (e.getAction() == MotionEvent.ACTION_UP) {
                float cx = getWidth() / 2f;
                float cy = getHeight() / 2f;
                float dx = e.getX() - cx;
                float dy = e.getY() - cy;

                if (Math.sqrt(dx * dx + dy * dy) <= dp(48) && canSpin()) {
                    performClick();
                    MainActivity.this.spin();
                    return true;
                }
            }
            return true;
        }

        @Override
        public boolean performClick() {
            super.performClick();
            return true;
        }

        void spinTo(int index, Runnable done) {
            if (members.size() < 2) return;

            if (animator != null) animator.cancel();

            finishAction = done;

            float slice = 360f / members.size();
            float target = (360f - ((index + .5f) * slice)) % 360f;

            float current = rotation % 360f;
            if (current < 0) current += 360f;

            float delta = target - current;
            if (delta < 0) delta += 360f;

            final float start = rotation;
            final float end = rotation + (360f * 8f) + delta;

            animator = ValueAnimator.ofFloat(start, end);
            animator.setDuration(5200);
            animator.setInterpolator(new DecelerateInterpolator(2.6f));

            animator.addUpdateListener(a -> {
                rotation = (Float) a.getAnimatedValue();
                invalidate();
            });

            animator.addListener(new AnimatorListenerAdapter() {
                @Override
                public void onAnimationEnd(Animator a) {
                    if (animator != a) return;

                    animator = null;
                    rotation = end % 360f;
                    invalidate();

                    if (finishAction != null) {
                        Runnable r = finishAction;
                        finishAction = null;
                        r.run();
                    }
                }

                @Override
                public void onAnimationCancel(Animator a) {
                    if (animator == a) {
                        animator = null;
                        finishAction = null;
                    }
                }
            });

            animator.start();
        }

        void stopAnimation() {
            if (animator != null) animator.cancel();
            animator = null;
            finishAction = null;
        }

        @Override
        protected void onDraw(Canvas canvas) {
            super.onDraw(canvas);

            float cx = getWidth() / 2f;
            float cy = getHeight() / 2f;
            float radius = Math.min(getWidth(), getHeight()) * .40f;

            paint.setStyle(Paint.Style.FILL);
            paint.setColor(Color.rgb(50, 52, 62));
            paint.setShadowLayer(dp(12), 0, dp(6), 0x55000000);
            canvas.drawCircle(cx, cy, radius + dp(8), paint);
            paint.clearShadowLayer();

            if (members.isEmpty()) {
                paint.setColor(Color.rgb(225, 227, 235));
                canvas.drawCircle(cx, cy, radius, paint);
            } else {
                float slice = 360f / members.size();
                RectF rect = new RectF(cx-radius, cy-radius, cx+radius, cy+radius);

                canvas.save();
                canvas.rotate(rotation, cx, cy);

                for (int i = 0; i < members.size(); i++) {
                    float start = -90f + i * slice;

                    paint.setStyle(Paint.Style.FILL);
                    paint.setColor(colors[i % colors.length]);
                    canvas.drawArc(rect, start, slice, true, paint);

                    paint.setStyle(Paint.Style.STROKE);
                    paint.setStrokeWidth(dp(2));
                    paint.setColor(0x99FFFFFF);
                    canvas.drawArc(rect, start, slice, true, paint);

                    drawMember(canvas, members.get(i), cx, cy, radius,
                            start + slice / 2f);
                }

                canvas.restore();
            }

            // Center SPIN control
            paint.setStyle(Paint.Style.FILL);
            paint.setColor(Color.WHITE);
            paint.setShadowLayer(dp(8), 0, dp(3), 0x66000000);
            canvas.drawCircle(cx, cy, dp(48), paint);
            paint.clearShadowLayer();

            paint.setColor(Color.rgb(30, 32, 42));
            canvas.drawCircle(cx, cy, dp(38), paint);

            textPaint.setColor(Color.WHITE);
            textPaint.setTextAlign(Paint.Align.CENTER);
            textPaint.setTextSize(dp(13));
            canvas.drawText("SPIN", cx, cy + dp(5), textPaint);

            // Fixed pointer at the top.
            Path pointer = new Path();
            pointer.moveTo(cx - dp(18), dp(5));
            pointer.lineTo(cx + dp(18), dp(5));
            pointer.lineTo(cx, dp(38));
            pointer.close();

            paint.setColor(Color.rgb(25, 25, 32));
            paint.setStyle(Paint.Style.FILL);
            canvas.drawPath(pointer, paint);
        }

        void drawMember(Canvas canvas, String name, float cx, float cy,
                        float radius, float angle) {

            double rad = Math.toRadians(angle);
            float tx = cx + (float)Math.cos(rad) * radius * .63f;
            float ty = cy + (float)Math.sin(rad) * radius * .63f;

            canvas.save();
            canvas.rotate(angle + 90f, tx, ty);

            textPaint.setColor(Color.WHITE);
            textPaint.setTextAlign(Paint.Align.CENTER);

            int size = 14;
            if (members.size() > 15) size = 11;
            if (members.size() > 25) size = 9;

            textPaint.setTextSize(dp(size));

            String shown = name;
            if (shown.length() > 14) shown = shown.substring(0, 13) + "…";

            canvas.drawText(shown, tx, ty, textPaint);
            canvas.restore();
        }
    }
}
