package de.niklaskorz.lgvertretungsplan;

import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import butterknife.ButterKnife;
import butterknife.InjectView;
import butterknife.OnClick;

/**
 * Created by niklaskorz on 19.04.15.
 */
public class PlanAdapter extends RecyclerView.Adapter<PlanAdapter.ViewHolder> implements View.OnClickListener {
    Plan plan;
    LinearLayoutManager layoutManager;
    int expandedPosition = -1;

    public static class ViewHolder extends RecyclerView.ViewHolder {
        View view;

        @InjectView(R.id.text_subject) TextView subjectView;
        @InjectView(R.id.text_time) TextView timeView;
        @InjectView(R.id.text_classes) TextView classesView;
        @InjectView(R.id.text_type) TextView typeView;

        @InjectView(R.id.label_substituteTeacher) TextView labelSubstituteTeacher;
        @InjectView(R.id.label_substituteSubject) TextView labelSubstituteSubject;
        @InjectView(R.id.label_room) TextView labelRoom;
        @InjectView(R.id.label_replaces) TextView labelReplaces;
        @InjectView(R.id.label_text) TextView labelText;

        @InjectView(R.id.text_substituteTeacher) TextView textSubstituteTeacher;
        @InjectView(R.id.text_substituteSubject) TextView textSubstituteSubject;
        @InjectView(R.id.text_room) TextView textRoom;
        @InjectView(R.id.text_replaces) TextView textReplaces;
        @InjectView(R.id.text_text) TextView textText;

        @InjectView(R.id.text_info) TextView textInfo;

        @InjectView(R.id.detail) ViewGroup detailView;

        boolean hasDetails = false;

        public ViewHolder(View v) {
            super(v);
            view = v;
            view.setTag(this);
            ButterKnife.inject(this, view);
        }

        public void expand() {
            detailView.setVisibility(View.VISIBLE);
        }

        public void collapse() {
            detailView.setVisibility(View.GONE);
        }

        public void update(PlanEntry e) {
            // Header
            if (e.subject.isEmpty()) {
                subjectView.setText(e.type);
                typeView.setText("");
            } else {
                subjectView.setText(e.subject);
                typeView.setText(e.type);
            }

            timeView.setText(e.hours);
            classesView.setText(e.classes);
            if (e.hours.length() > 1) {
                timeView.setTextSize(TypedValue.COMPLEX_UNIT_SP, 15);
            } else {
                timeView.setTextSize(TypedValue.COMPLEX_UNIT_SP, 25);
            }

            // Detail
            hasDetails = false;

            if (e.substituteTeacher.isEmpty()) {
                labelSubstituteTeacher.setVisibility(View.GONE);
                textSubstituteTeacher.setVisibility(View.GONE);
            } else {
                hasDetails = true;
                labelSubstituteTeacher.setVisibility(View.VISIBLE);
                textSubstituteTeacher.setVisibility(View.VISIBLE);
            }
            textSubstituteTeacher.setText(e.substituteTeacher);

            if (e.substituteSubject.isEmpty()) {
                labelSubstituteSubject.setVisibility(View.GONE);
                textSubstituteSubject.setVisibility(View.GONE);
            } else {
                hasDetails = true;
                labelSubstituteSubject.setVisibility(View.VISIBLE);
                textSubstituteSubject.setVisibility(View.VISIBLE);
            }
            textSubstituteSubject.setText(e.substituteSubject);

            if (e.room.isEmpty()) {
                labelRoom.setVisibility(View.GONE);
                textRoom.setVisibility(View.GONE);
            } else {
                hasDetails = true;
                labelRoom.setVisibility(View.VISIBLE);
                textRoom.setVisibility(View.VISIBLE);
            }
            textRoom.setText(e.room);

            if (e.replaces.isEmpty()) {
                labelReplaces.setVisibility(View.GONE);
                textReplaces.setVisibility(View.GONE);
            } else {
                hasDetails = true;
                labelReplaces.setVisibility(View.VISIBLE);
                textReplaces.setVisibility(View.VISIBLE);
            }
            textReplaces.setText(e.replaces);

            if (e.text.isEmpty()) {
                labelText.setVisibility(View.GONE);
                textText.setVisibility(View.GONE);
            } else {
                hasDetails = true;
                labelText.setVisibility(View.VISIBLE);
                textText.setVisibility(View.VISIBLE);
            }
            textText.setText(e.text);

            if (hasDetails) {
                textInfo.setVisibility(View.GONE);
            } else {
                textInfo.setVisibility(View.VISIBLE);
            }
        }
    }

    public PlanAdapter(LinearLayoutManager lm, Plan p) {
        layoutManager = lm;
        plan = p;
    }

    @Override
    public PlanAdapter.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.plan_item, parent, false);
        ViewHolder vh = new ViewHolder(v);
        vh.view.setOnClickListener(this);
        return vh;
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        holder.update(plan.entries.get(position));

        if (position == expandedPosition) {
            holder.expand();
        } else {
            holder.collapse();
        }
    }

    @Override
    public int getItemCount() {
        return plan.entries.size();
    }

    @Override
    public void onClick(View v) {
        ViewHolder holder = (ViewHolder) v.getTag();

        int prev = -1;
        if (expandedPosition >= 0) {
            prev = expandedPosition;
            notifyItemChanged(prev);
        }
        expandedPosition = holder.getAdapterPosition();
        if (expandedPosition == prev) {
            expandedPosition = -1;
        } else {
            layoutManager.scrollToPositionWithOffset(holder.getLayoutPosition(), 0);
            notifyItemChanged(expandedPosition);
        }
    }
}
