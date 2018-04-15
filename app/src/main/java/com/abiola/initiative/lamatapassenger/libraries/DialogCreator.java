package com.abiola.initiative.lamatapassenger.libraries;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.view.LayoutInflater;
import android.view.View;


public final class DialogCreator {
    private final Context context;
    private final String actionId;
    private final AlertDialog.Builder builder;

    private DialogCreator(Context context, String actionId) {
        this.context = context;
        this.actionId = actionId;
        builder = new AlertDialog.Builder(context);
    }

    public interface DialogActionListener {
        void onClickPositiveButton(String actionId);
        void onClickNegativeButton(String actionId);
        void onClickNeutralButton(String actionId);
        void onClickMultiChoiceItem(String actionId, int which, boolean isChecked);
        void onCreateDialogView(String actionId, View view);
    }

    public DialogCreator setTitle(int titleId) {
        builder.setTitle(titleId);
        return this;
    }

    public DialogCreator setTitle(String title) {
        builder.setTitle(title);
        return this;
    }

    public DialogCreator setCancelable(boolean cancelable) {
        builder.setCancelable(cancelable);
        return this;
    }

    public DialogCreator setPositiveButton(int textId) {
        builder.setPositiveButton(textId, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                ((DialogActionListener) context).onClickPositiveButton(actionId);
            }
        });
        return this;
    }

    public DialogCreator setPositiveButton(String text) {
        builder.setPositiveButton(text, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                ((DialogActionListener) context).onClickPositiveButton(actionId);
            }
        });
        return this;
    }

    public DialogCreator setNegativeButton(int textId) {
        builder.setNegativeButton(textId, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                ((DialogActionListener) context).onClickNegativeButton(actionId);
            }
        });
        return this;
    }

    public DialogCreator setNegativeButton(String text) {
        builder.setNegativeButton(text, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                ((DialogActionListener) context).onClickNegativeButton(actionId);
            }
        });
        return this;
    }

    public DialogCreator setNeutralButton(int textId) {
        builder.setNeutralButton(textId, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                ((DialogActionListener) context).onClickNeutralButton(actionId);
            }
        });
        return this;
    }

    public DialogCreator setNeutralButton(String text) {
        builder.setNeutralButton(text, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                ((DialogActionListener) context).onClickNeutralButton(actionId);
            }
        });
        return this;
    }

    public DialogCreator setMessage(String message) {
        builder.setMessage(message);
        return this;
    }

    public DialogCreator setMessage(int messageId) {
        builder.setMessage(messageId);
        return this;
    }

    public DialogCreator setMultiChoiceItems(String[] list, boolean[] checkedItems) {
        builder.setMultiChoiceItems(list, checkedItems, new DialogInterface.OnMultiChoiceClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which, boolean isChecked) {
                ((DialogActionListener) context).onClickMultiChoiceItem(actionId, which, isChecked);
            }
        });
        return this;
    }

    public DialogCreator setView(int id) {
        LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View view = inflater.inflate(id, null);
        builder.setView(view);
        ((DialogActionListener) context).onCreateDialogView(actionId, view);
        return this;
    }

    public DialogCreator show() {
        builder.show();
        return this;
    }

    public static DialogCreator create(Context context, String actionId) {
        return new DialogCreator(context, actionId);
    }
}
