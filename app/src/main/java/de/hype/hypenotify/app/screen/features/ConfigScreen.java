package de.hype.hypenotify.app.screen.features;

import android.annotation.SuppressLint;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.*;
import androidx.appcompat.app.AlertDialog;
import de.hype.hypenotify.Config;
import de.hype.hypenotify.ConfigField;
import de.hype.hypenotify.R;
import de.hype.hypenotify.app.core.interfaces.Core;
import de.hype.hypenotify.app.screen.Screen;
import de.hype.hypenotify.layouts.autodetection.Layout;

import java.lang.reflect.Field;
import java.time.Instant;
import java.util.Arrays;

@Layout(name = "Config")
@SuppressLint("ViewConstructor")
public class ConfigScreen extends Screen {
    private final Config configObject;

    public ConfigScreen(Core core, View parent) {
        super(core, parent);
        this.configObject = core.config();
        inflateLayouts();
    }

    @Override
    protected void inflateLayouts() {
        setOrientation(VERTICAL);
        LayoutInflater.from(context).inflate(R.layout.config_screen, this, true);
        LinearLayout layout = findViewById(R.id.config_layout);
        layout.removeAllViews();
        populateConfigFields(layout);
    }

    private void populateConfigFields(LinearLayout layout) {
        Field[] fields = configObject.getClass().getDeclaredFields();
        for (Field field : fields) {
            field.setAccessible(true);
            ConfigField configField = field.getAnnotation(ConfigField.class);
            if (configField != null) {
                try {
                    Class<?> fieldType = field.getType();
                    Object value = field.get(configObject);
                    if (fieldType == Boolean.class || fieldType == boolean.class) {
                        addSwitch(layout, field, (Boolean) value, configField);
                    } else if (fieldType == String.class) {
                        addTextField(layout, field, (String) value, configField);
                    } else if (fieldType == Integer.class || fieldType == int.class) {
                        addNumberInput(layout, field, (Integer) value, configField);
                    } else if (fieldType == Long.class || fieldType == long.class) {
                        addLongInput(layout, field, (Long) value, configField);
                    } else if (fieldType == Double.class || fieldType == double.class) {
                        addDoubleInput(layout, field, (Double) value, configField);
                    } else if (fieldType == Instant.class) {
                        addTimeInput(layout, field, (Instant) value, configField);
                    } else if (fieldType.isEnum()) {
                        addEnumSelection(layout, field, (Enum<?>) value, configField);
                    }
                } catch (IllegalAccessException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    private void addSwitch(LinearLayout layout, Field field, Boolean value, ConfigField configField) {
        LinearLayout fieldLayout = new LinearLayout(context);
        fieldLayout.setOrientation(LinearLayout.HORIZONTAL);

        TextView label = new TextView(context);
        label.setText(configField.description().isEmpty() ? field.getName() : configField.description());
        LinearLayout.LayoutParams labelParams = new LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f);
        label.setLayoutParams(labelParams);
        fieldLayout.addView(label);

        Switch switchView = new Switch(context);
        switchView.setChecked(value != null && value);
        switchView.setOnCheckedChangeListener((buttonView, isChecked) -> {
            try {
                field.set(configObject, isChecked);
            } catch (IllegalAccessException e) {
                e.printStackTrace();
            }
        });
        fieldLayout.addView(switchView);

        if (configField.allowNull()) {
            Button nullButton = new Button(context);
            nullButton.setText("Set Null");
            nullButton.setOnClickListener(v -> {
                try {
                    field.set(configObject, null);
                    switchView.setChecked(false);
                } catch (IllegalAccessException e) {
                    e.printStackTrace();
                }
            });
            fieldLayout.addView(nullButton);
        }

        layout.addView(fieldLayout);
    }

    private void addTextField(LinearLayout layout, Field field, String value, ConfigField configField) {
        LinearLayout fieldLayout = new LinearLayout(context);
        fieldLayout.setOrientation(LinearLayout.HORIZONTAL);

        TextView label = new TextView(context);
        label.setText(configField.description().isEmpty() ? field.getName() : configField.description());
        LinearLayout.LayoutParams labelParams = new LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f);
        label.setLayoutParams(labelParams);
        fieldLayout.addView(label);

        EditText editText = new EditText(context);
        editText.setText(value);
        editText.setHint(field.getName());
        LinearLayout.LayoutParams editTextParams = new LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 2f);
        editText.setLayoutParams(editTextParams);
        editText.setOnFocusChangeListener((v, hasFocus) -> {
            if (!hasFocus) {
                try {
                    String text = editText.getText().toString();
                    field.set(configObject, text.isEmpty() ? null : text);
                } catch (IllegalAccessException e) {
                    e.printStackTrace();
                }
            }
        });
        fieldLayout.addView(editText);

        if (configField.allowNull()) {
            Button nullButton = new Button(context);
            nullButton.setText("Set Null");
            nullButton.setOnClickListener(v -> {
                try {
                    field.set(configObject, null);
                    editText.setText("");
                } catch (IllegalAccessException e) {
                    e.printStackTrace();
                }
            });
            fieldLayout.addView(nullButton);
        }

        layout.addView(fieldLayout);
    }

    private void addNumberInput(LinearLayout layout, Field field, Integer value, ConfigField configField) {
        LinearLayout fieldLayout = new LinearLayout(context);
        fieldLayout.setOrientation(LinearLayout.HORIZONTAL);

        TextView label = new TextView(context);
        label.setText(configField.description().isEmpty() ? field.getName() : configField.description());
        LinearLayout.LayoutParams labelParams = new LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f);
        label.setLayoutParams(labelParams);
        fieldLayout.addView(label);

        EditText editText = new EditText(context);
        editText.setText(value != null ? String.valueOf(value) : "");
        editText.setInputType(android.text.InputType.TYPE_CLASS_NUMBER);
        LinearLayout.LayoutParams editTextParams = new LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 2f);
        editText.setLayoutParams(editTextParams);
        editText.setOnFocusChangeListener((v, hasFocus) -> {
            if (!hasFocus) {
                try {
                    String text = editText.getText().toString();
                    field.set(configObject, text.isEmpty() ? null : Integer.parseInt(text));
                } catch (IllegalAccessException e) {
                    e.printStackTrace();
                }
            }
        });
        fieldLayout.addView(editText);

        if (configField.allowNull()) {
            Button nullButton = new Button(context);
            nullButton.setText("Set Null");
            nullButton.setOnClickListener(v -> {
                try {
                    field.set(configObject, null);
                    editText.setText("");
                } catch (IllegalAccessException e) {
                    e.printStackTrace();
                }
            });
            fieldLayout.addView(nullButton);
        }

        layout.addView(fieldLayout);
    }

    private void addLongInput(LinearLayout layout, Field field, Long value, ConfigField configField) {
        LinearLayout fieldLayout = new LinearLayout(context);
        fieldLayout.setOrientation(LinearLayout.HORIZONTAL);

        TextView label = new TextView(context);
        label.setText(configField.description().isEmpty() ? field.getName() : configField.description());
        LinearLayout.LayoutParams labelParams = new LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f);
        label.setLayoutParams(labelParams);
        fieldLayout.addView(label);

        EditText editText = new EditText(context);
        editText.setText(value != null ? String.valueOf(value) : "");
        editText.setInputType(android.text.InputType.TYPE_CLASS_NUMBER);
        LinearLayout.LayoutParams editTextParams = new LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 2f);
        editText.setLayoutParams(editTextParams);
        editText.setOnFocusChangeListener((v, hasFocus) -> {
            if (!hasFocus) {
                try {
                    String text = editText.getText().toString();
                    field.set(configObject, text.isEmpty() ? null : Long.parseLong(text));
                } catch (IllegalAccessException e) {
                    e.printStackTrace();
                }
            }
        });
        fieldLayout.addView(editText);

        if (configField.allowNull()) {
            Button nullButton = new Button(context);
            nullButton.setText("Set Null");
            nullButton.setOnClickListener(v -> {
                try {
                    field.set(configObject, null);
                    editText.setText("");
                } catch (IllegalAccessException e) {
                    e.printStackTrace();
                }
            });
            fieldLayout.addView(nullButton);
        }

        layout.addView(fieldLayout);
    }

    private void addDoubleInput(LinearLayout layout, Field field, Double value, ConfigField configField) {
        LinearLayout fieldLayout = new LinearLayout(context);
        fieldLayout.setOrientation(LinearLayout.HORIZONTAL);

        TextView label = new TextView(context);
        label.setText(configField.description().isEmpty() ? field.getName() : configField.description());
        LinearLayout.LayoutParams labelParams = new LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f);
        label.setLayoutParams(labelParams);
        fieldLayout.addView(label);

        EditText editText = new EditText(context);
        editText.setText(value != null ? String.valueOf(value) : "");
        editText.setInputType(android.text.InputType.TYPE_CLASS_NUMBER | android.text.InputType.TYPE_NUMBER_FLAG_DECIMAL);
        LinearLayout.LayoutParams editTextParams = new LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 2f);
        editText.setLayoutParams(editTextParams);
        editText.setOnFocusChangeListener((v, hasFocus) -> {
            if (!hasFocus) {
                try {
                    String text = editText.getText().toString();
                    field.set(configObject, text.isEmpty() ? null : Double.parseDouble(text));
                } catch (IllegalAccessException e) {
                    e.printStackTrace();
                }
            }
        });
        fieldLayout.addView(editText);

        if (configField.allowNull()) {
            Button nullButton = new Button(context);
            nullButton.setText("Set Null");
            nullButton.setOnClickListener(v -> {
                try {
                    field.set(configObject, null);
                    editText.setText("");
                } catch (IllegalAccessException e) {
                    e.printStackTrace();
                }
            });
            fieldLayout.addView(nullButton);
        }

        layout.addView(fieldLayout);
    }

    private void addTimeInput(LinearLayout layout, Field field, Instant value, ConfigField configField) {
        LinearLayout fieldLayout = new LinearLayout(context);
        fieldLayout.setOrientation(LinearLayout.HORIZONTAL);

        TextView label = new TextView(context);
        label.setText(configField.description().isEmpty() ? field.getName() : configField.description());
        LinearLayout.LayoutParams labelParams = new LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f);
        label.setLayoutParams(labelParams);
        fieldLayout.addView(label);

        TextView timeView = new TextView(context);
        timeView.setText(value != null ? value.toString() : "null");
        LinearLayout.LayoutParams timeViewParams = new LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 2f);
        timeView.setLayoutParams(timeViewParams);
        timeView.setOnClickListener(v -> {
            // Show time picker dialog and update the field
            Toast.makeText(context, "Time picker not implemented", Toast.LENGTH_SHORT).show();
        });
        fieldLayout.addView(timeView);

        if (configField.allowNull()) {
            Button nullButton = new Button(context);
            nullButton.setText("Set Null");
            nullButton.setOnClickListener(v -> {
                try {
                    field.set(configObject, null);
                    timeView.setText("null");
                } catch (IllegalAccessException e) {
                    e.printStackTrace();
                }
            });
            fieldLayout.addView(nullButton);
        }

        layout.addView(fieldLayout);
    }

    private void addEnumSelection(LinearLayout layout, Field field, Enum<?> value, ConfigField configField) {
        LinearLayout fieldLayout = new LinearLayout(context);
        fieldLayout.setOrientation(LinearLayout.HORIZONTAL);

        TextView label = new TextView(context);
        label.setText(configField.description().isEmpty() ? field.getName() : configField.description());
        LinearLayout.LayoutParams labelParams = new LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f);
        label.setLayoutParams(labelParams);
        fieldLayout.addView(label);

        TextView enumView = new TextView(context);
        enumView.setText(value != null ? value.name() : "null");
        LinearLayout.LayoutParams enumViewParams = new LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 2f);
        enumView.setLayoutParams(enumViewParams);
        enumView.setOnClickListener(v -> {
            AlertDialog.Builder builder = new AlertDialog.Builder(context);
            builder.setTitle("Select " + field.getName());
            String[] enumNames = Arrays.stream(field.getType().getEnumConstants())
                    .map(Object::toString)
                    .toArray(String[]::new);
            if (configField.allowNull()) {
                enumNames = Arrays.copyOf(enumNames, enumNames.length + 1);
                enumNames[enumNames.length - 1] = "null";
            }
            String[] finalEnumNames = enumNames;
            builder.setItems(enumNames, (dialog, which) -> {
                try {
                    field.set(configObject, which == finalEnumNames.length - 1 && configField.allowNull() ? null : field.getType().getEnumConstants()[which]);
                    enumView.setText(finalEnumNames[which]);
                } catch (IllegalAccessException e) {
                    e.printStackTrace();
                }
            });
            builder.show();
        });
        fieldLayout.addView(enumView);

        layout.addView(fieldLayout);
    }

    @Override
    protected void updateScreen(LinearLayout dynamicScreen) {
        // No dynamic updates needed for this screen
    }

    @Override
    public void onPause() {

    }

    @Override
    public void onResume() {

    }

    @Override
    protected LinearLayout getDynamicScreen() {
        return null;
    }
}