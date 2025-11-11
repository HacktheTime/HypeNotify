package de.hype.hypenotify.app.screen.features

import android.annotation.SuppressLint
import android.content.DialogInterface
import android.text.InputType
import android.view.LayoutInflater
import android.view.View
import android.widget.Button
import android.widget.CompoundButton
import android.widget.EditText
import android.widget.LinearLayout
import android.widget.Switch
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import de.hype.hypenotify.R
import de.hype.hypenotify.app.Config
import de.hype.hypenotify.app.ConfigField
import de.hype.hypenotify.app.core.interfaces.Core
import de.hype.hypenotify.app.screen.Screen
import de.hype.hypenotify.layouts.autodetection.Layout
import java.lang.reflect.Field
import java.time.Instant
import java.util.*

@Layout(name = "Config")
@SuppressLint("ViewConstructor")
class ConfigScreen(core: Core, parent: View?) : Screen(core, parent) {
    private val configObject: Config = core.config()

    init {
        inflateLayouts()
    }

    override fun inflateLayouts() {
        setOrientation(VERTICAL)
        LayoutInflater.from(context).inflate(R.layout.config_screen, this, true)
        val layout = findViewById<LinearLayout>(R.id.config_layout)
        layout.removeAllViews()
        populateConfigFields(layout)
    }

    private fun populateConfigFields(layout: LinearLayout) {
        val fields = configObject.javaClass.getDeclaredFields()
        for (field in fields) {
            field.setAccessible(true)
            val configField = field.getAnnotation<ConfigField?>(ConfigField::class.java)
            if (configField != null) {
                try {
                    val fieldType = field.getType()
                    val value = field.get(configObject)
                    if (fieldType == Boolean::class.java || fieldType == Boolean::class.javaPrimitiveType) {
                        addSwitch(layout, field, value as Boolean?, configField)
                    } else if (fieldType == String::class.java) {
                        addTextField(layout, field, value as String?, configField)
                    } else if (fieldType == Int::class.java || fieldType == Int::class.javaPrimitiveType) {
                        addNumberInput(layout, field, value as Int?, configField)
                    } else if (fieldType == Long::class.java || fieldType == Long::class.javaPrimitiveType) {
                        addLongInput(layout, field, value as Long?, configField)
                    } else if (fieldType == Double::class.java || fieldType == Double::class.javaPrimitiveType) {
                        addDoubleInput(layout, field, value as Double?, configField)
                    } else if (fieldType == Instant::class.java) {
                        addTimeInput(layout, field, value as Instant?, configField)
                    } else if (fieldType.isEnum()) {
                        addEnumSelection(layout, field, value as Enum<*>?, configField)
                    }
                } catch (e: IllegalAccessException) {
                    e.printStackTrace()
                }
            }
        }
    }

    private fun addSwitch(layout: LinearLayout, field: Field, value: Boolean?, configField: ConfigField) {
        val fieldLayout = LinearLayout(context)
        fieldLayout.setOrientation(HORIZONTAL)

        val label = TextView(context)
        label.setText(if (configField.description.isEmpty()) field.getName() else configField.description)
        val labelParams = LayoutParams(0, LayoutParams.WRAP_CONTENT, 1f)
        label.setLayoutParams(labelParams)
        fieldLayout.addView(label)

        val switchView = Switch(context)
        switchView.setChecked(value != null && value)
        switchView.setOnCheckedChangeListener(CompoundButton.OnCheckedChangeListener { buttonView: CompoundButton?, isChecked: Boolean ->
            try {
                field.set(configObject, isChecked)
            } catch (e: IllegalAccessException) {
                e.printStackTrace()
            }
        })
        fieldLayout.addView(switchView)

        if (configField.allowNull) {
            val nullButton = Button(context)
            nullButton.setText("Set Null")
            nullButton.setOnClickListener(OnClickListener { v: View? ->
                try {
                    field.set(configObject, null)
                    switchView.setChecked(false)
                } catch (e: IllegalAccessException) {
                    e.printStackTrace()
                }
            })
            fieldLayout.addView(nullButton)
        }

        layout.addView(fieldLayout)
    }

    private fun addTextField(layout: LinearLayout, field: Field, value: String?, configField: ConfigField) {
        val fieldLayout = LinearLayout(context)
        fieldLayout.setOrientation(HORIZONTAL)

        val label = TextView(context)
        label.setText(if (configField.description.isEmpty()) field.getName() else configField.description)
        val labelParams = LayoutParams(0, LayoutParams.WRAP_CONTENT, 1f)
        label.setLayoutParams(labelParams)
        fieldLayout.addView(label)

        val editText = EditText(context)
        editText.setText(value)
        editText.setHint(field.getName())
        val editTextParams = LayoutParams(0, LayoutParams.WRAP_CONTENT, 2f)
        editText.setLayoutParams(editTextParams)
        editText.setOnFocusChangeListener(OnFocusChangeListener { v: View?, hasFocus: Boolean ->
            if (!hasFocus) {
                try {
                    val text = editText.getText().toString()
                    field.set(configObject, if (text.isEmpty()) null else text)
                } catch (e: IllegalAccessException) {
                    e.printStackTrace()
                }
            }
        })
        fieldLayout.addView(editText)

        if (configField.allowNull) {
            val nullButton = Button(context)
            nullButton.setText("Set Null")
            nullButton.setOnClickListener(OnClickListener { v: View? ->
                try {
                    field.set(configObject, null)
                    editText.setText("")
                } catch (e: IllegalAccessException) {
                    e.printStackTrace()
                }
            })
            fieldLayout.addView(nullButton)
        }

        layout.addView(fieldLayout)
    }

    private fun addNumberInput(layout: LinearLayout, field: Field, value: Int?, configField: ConfigField) {
        val fieldLayout = LinearLayout(context)
        fieldLayout.setOrientation(HORIZONTAL)

        val label = TextView(context)
        label.setText(if (configField.description.isEmpty()) field.getName() else configField.description)
        val labelParams = LayoutParams(0, LayoutParams.WRAP_CONTENT, 1f)
        label.setLayoutParams(labelParams)
        fieldLayout.addView(label)

        val editText = EditText(context)
        editText.setText(if (value != null) value.toString() else "")
        editText.setInputType(InputType.TYPE_CLASS_NUMBER)
        val editTextParams = LayoutParams(0, LayoutParams.WRAP_CONTENT, 2f)
        editText.setLayoutParams(editTextParams)
        editText.setOnFocusChangeListener(OnFocusChangeListener { v: View?, hasFocus: Boolean ->
            if (!hasFocus) {
                try {
                    val text = editText.getText().toString()
                    field.set(configObject, if (text.isEmpty()) null else text.toInt())
                } catch (e: IllegalAccessException) {
                    e.printStackTrace()
                }
            }
        })
        fieldLayout.addView(editText)

        if (configField.allowNull) {
            val nullButton = Button(context)
            nullButton.setText("Set Null")
            nullButton.setOnClickListener(OnClickListener { v: View? ->
                try {
                    field.set(configObject, null)
                    editText.setText("")
                } catch (e: IllegalAccessException) {
                    e.printStackTrace()
                }
            })
            fieldLayout.addView(nullButton)
        }

        layout.addView(fieldLayout)
    }

    private fun addLongInput(layout: LinearLayout, field: Field, value: Long?, configField: ConfigField) {
        val fieldLayout = LinearLayout(context)
        fieldLayout.setOrientation(HORIZONTAL)

        val label = TextView(context)
        label.setText(if (configField.description.isEmpty()) field.getName() else configField.description)
        val labelParams = LayoutParams(0, LayoutParams.WRAP_CONTENT, 1f)
        label.setLayoutParams(labelParams)
        fieldLayout.addView(label)

        val editText = EditText(context)
        editText.setText(if (value != null) value.toString() else "")
        editText.setInputType(InputType.TYPE_CLASS_NUMBER)
        val editTextParams = LayoutParams(0, LayoutParams.WRAP_CONTENT, 2f)
        editText.setLayoutParams(editTextParams)
        editText.setOnFocusChangeListener(OnFocusChangeListener { v: View?, hasFocus: Boolean ->
            if (!hasFocus) {
                try {
                    val text = editText.getText().toString()
                    field.set(configObject, if (text.isEmpty()) null else text.toLong())
                } catch (e: IllegalAccessException) {
                    e.printStackTrace()
                }
            }
        })
        fieldLayout.addView(editText)

        if (configField.allowNull) {
            val nullButton = Button(context)
            nullButton.setText("Set Null")
            nullButton.setOnClickListener(OnClickListener { v: View? ->
                try {
                    field.set(configObject, null)
                    editText.setText("")
                } catch (e: IllegalAccessException) {
                    e.printStackTrace()
                }
            })
            fieldLayout.addView(nullButton)
        }

        layout.addView(fieldLayout)
    }

    private fun addDoubleInput(layout: LinearLayout, field: Field, value: Double?, configField: ConfigField) {
        val fieldLayout = LinearLayout(context)
        fieldLayout.setOrientation(HORIZONTAL)

        val label = TextView(context)
        label.setText(if (configField.description.isEmpty()) field.getName() else configField.description)
        val labelParams = LayoutParams(0, LayoutParams.WRAP_CONTENT, 1f)
        label.setLayoutParams(labelParams)
        fieldLayout.addView(label)

        val editText = EditText(context)
        editText.setText(if (value != null) value.toString() else "")
        editText.setInputType(InputType.TYPE_CLASS_NUMBER or InputType.TYPE_NUMBER_FLAG_DECIMAL)
        val editTextParams = LayoutParams(0, LayoutParams.WRAP_CONTENT, 2f)
        editText.setLayoutParams(editTextParams)
        editText.setOnFocusChangeListener(OnFocusChangeListener { v: View?, hasFocus: Boolean ->
            if (!hasFocus) {
                try {
                    val text = editText.getText().toString()
                    field.set(configObject, if (text.isEmpty()) null else text.toDouble())
                } catch (e: IllegalAccessException) {
                    e.printStackTrace()
                }
            }
        })
        fieldLayout.addView(editText)

        if (configField.allowNull) {
            val nullButton = Button(context)
            nullButton.setText("Set Null")
            nullButton.setOnClickListener(OnClickListener { v: View? ->
                try {
                    field.set(configObject, null)
                    editText.setText("")
                } catch (e: IllegalAccessException) {
                    e.printStackTrace()
                }
            })
            fieldLayout.addView(nullButton)
        }

        layout.addView(fieldLayout)
    }

    private fun addTimeInput(layout: LinearLayout, field: Field, value: Instant?, configField: ConfigField) {
        val fieldLayout = LinearLayout(context)
        fieldLayout.setOrientation(HORIZONTAL)

        val label = TextView(context)
        label.setText(if (configField.description.isEmpty()) field.getName() else configField.description)
        val labelParams = LayoutParams(0, LayoutParams.WRAP_CONTENT, 1f)
        label.setLayoutParams(labelParams)
        fieldLayout.addView(label)

        val timeView = TextView(context)
        timeView.setText(if (value != null) value.toString() else "null")
        val timeViewParams = LayoutParams(0, LayoutParams.WRAP_CONTENT, 2f)
        timeView.setLayoutParams(timeViewParams)
        timeView.setOnClickListener(OnClickListener { v: View? ->
            // Show time picker dialog and update the field
            Toast.makeText(context, "Time picker not implemented", Toast.LENGTH_SHORT).show()
        })
        fieldLayout.addView(timeView)

        if (configField.allowNull) {
            val nullButton = Button(context)
            nullButton.setText("Set Null")
            nullButton.setOnClickListener(OnClickListener { v: View? ->
                try {
                    field.set(configObject, null)
                    timeView.setText("null")
                } catch (e: IllegalAccessException) {
                    e.printStackTrace()
                }
            })
            fieldLayout.addView(nullButton)
        }

        layout.addView(fieldLayout)
    }

    private fun addEnumSelection(layout: LinearLayout, field: Field, value: Enum<*>?, configField: ConfigField) {
        val fieldLayout = LinearLayout(context)
        fieldLayout.setOrientation(HORIZONTAL)

        val label = TextView(context)
        label.setText(if (configField.description.isEmpty()) field.getName() else configField.description)
        val labelParams = LayoutParams(0, LayoutParams.WRAP_CONTENT, 1f)
        label.setLayoutParams(labelParams)
        fieldLayout.addView(label)

        val enumView = TextView(context)
        enumView.setText(if (value != null) value.name else "null")
        val enumViewParams = LayoutParams(0, LayoutParams.WRAP_CONTENT, 2f)
        enumView.setLayoutParams(enumViewParams)
        enumView.setOnClickListener(OnClickListener { v: View? ->
            val builder = AlertDialog.Builder(context)
            builder.setTitle("Select " + field.getName())
            var enumNames = Arrays.stream(field.getType().getEnumConstants())
                .map<String?> { obj: Any? -> obj.toString() }
                .toArray<String?> { _Dummy_.__Array__() }
            if (configField.allowNull) {
                enumNames = enumNames.copyOf<String?>(enumNames.size + 1)
                enumNames[enumNames.size - 1] = "null"
            }
            val finalEnumNames = enumNames
            builder.setItems(enumNames, DialogInterface.OnClickListener { dialog: DialogInterface?, which: Int ->
                try {
                    field.set(
                        configObject,
                        if (which == finalEnumNames.size - 1 && configField.allowNull) null else field.getType().getEnumConstants()[which]
                    )
                    enumView.setText(finalEnumNames[which])
                } catch (e: IllegalAccessException) {
                    e.printStackTrace()
                }
            })
            builder.show()
        })
        fieldLayout.addView(enumView)

        layout.addView(fieldLayout)
    }

    override fun updateScreen(dynamicScreen: LinearLayout) {
        // No dynamic updates needed for this screen
    }

    override fun onPause() {
    }

    override fun onResume() {
    }

    override fun getDynamicScreen(): LinearLayout? {
        return null
    }
}