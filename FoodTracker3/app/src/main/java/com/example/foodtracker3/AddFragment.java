package com.example.foodtracker3;

import android.app.DatePickerDialog;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;


import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

public class AddFragment extends Fragment {

    // View control objects --- Defining them here instead of in onCreateView(), so they can be accessed by validateInput() method.
    Button btn_add;
    EditText et_productName;
    EditText et_productQuantity;
    EditText et_unitAmount;
    EditText et_expirationDate;
    EditText et_dateAdded;
    Spinner sp_unit;
    Spinner sp_category;
    TextView et_newCategory;
    ImageView iv_cancelNewCategory;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState)
    {
        // Inflate the xml which gives us a view
        View view = inflater.inflate(R.layout.fragment_add, container, false);

        getActivity().getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_NOTHING); // fix keyboard/button issue (now button will stay put)

        // ES - I'm not sure what's ideal, as far as where to declare and initialize these variables, but this works.
        btn_add = view.findViewById(R.id.add_button);
        et_productName = view.findViewById(R.id.name_input);
        et_productQuantity = view.findViewById(R.id.quantity_input);
        et_unitAmount = view.findViewById(R.id.unitAmount_input);
        et_expirationDate = view.findViewById(R.id.tv_expirationDate);
        et_dateAdded = view.findViewById(R.id.tv_dateAdded);
        sp_unit = view.findViewById(R.id.unit_input);
        sp_category = view.findViewById(R.id.category_input);
        et_newCategory = view.findViewById(R.id.et_newCategory);
        iv_cancelNewCategory = view.findViewById(R.id.iv_cancelNewCategory);

        final DatabaseHelper dbh = new DatabaseHelper(getContext());      // is getContext() reliable, or will it sometimes return null? Research it more

        // Units setup
        final ArrayList<Unit> units = dbh.getUnits();
        List<String> spList_unitAbbrevs = new ArrayList<>();

        for (Unit i : units)    // Add unit abbreviation strings to spinner list
        {
            spList_unitAbbrevs.add(i.getAbbrev());
        }

        ArrayAdapter<String> unitAdapter = new ArrayAdapter<>(getContext(), R.layout.spinner_item, spList_unitAbbrevs);
        unitAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        sp_unit.setAdapter(unitAdapter);
        sp_unit.setSelection(0);    // unitAbbrevs[0] == "ct" (the default selection)

        // Categories setup
        final ArrayList<Category> categories = dbh.getCategories();
        final List<String> spList_categoryNames = new ArrayList<>();

        for (Category i : categories)
        {
            spList_categoryNames.add(i.getName());
        }
        spList_categoryNames.add("(Create New Category)");

        final ArrayAdapter<String> categoryAdapter = new ArrayAdapter<>(getContext(), R.layout.spinner_item, spList_categoryNames);
        categoryAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        sp_category.setAdapter(categoryAdapter);
        sp_category.setSelection(0);    // categories[0] == "None" (the default selection)

        AdapterView.OnItemSelectedListener OnItemSelListener_Spinner = new AdapterView.OnItemSelectedListener()
        {
            public void onItemSelected(AdapterView<?> parent, View view, int pos, long id)
            {
                if (pos == sp_category.getCount() - 1)      // if selected "Create New Category"
                {
                    showNewCategory(true);
                }
            }

            public void onNothingSelected(AdapterView<?> parent)
            {

            }
        };

        sp_category.setOnItemSelectedListener(OnItemSelListener_Spinner);

        iv_cancelNewCategory.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                showNewCategory(false);
            }
        });

        et_dateAdded.setText(DatabaseHelper.date_toAppStr(new Date()) );    // default text = the current date

        et_dateAdded.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                showDatePickerDialog(et_dateAdded);
            }
        });

        et_expirationDate.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
               showDatePickerDialog(et_expirationDate);
            }
        });


        btn_add.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                if (validateInput() == false)   return;
                if (et_newCategory.isEnabled())         // insert new category if necessary
                {
                    Category category = new Category(-1, et_newCategory.getText().toString(), "");
                    if (dbh.addCategory(category))
                    {
                        categories.add(category);
                        spList_categoryNames.add(spList_categoryNames.size() - 1, category.getName());
                        categoryAdapter.notifyDataSetChanged();
                    }
                    else
                    {
                        Toast.makeText(v.getContext(), "Insert failed", Toast.LENGTH_SHORT).show();
                        return;
                    }
                }

                Date addDate = DatabaseHelper.appStr_toDate(et_dateAdded.getText().toString());
                Date expDate = DatabaseHelper.appStr_toDate(et_expirationDate.getText().toString());
                long unitId = units.get(sp_unit.getSelectedItemPosition()).getId();
                long categoryId = categories.get(sp_category.getSelectedItemPosition()).getId();

                Product p = new Product
                (
                    -1,
                    et_productName.getText().toString(),
                    Integer.parseInt(et_productQuantity.getText().toString()),
                    unitId,
                    Double.parseDouble(et_unitAmount.getText().toString()),
                    addDate,
                    expDate,
                    (expDate == null) ? false : addDate.after(expDate),             // Determine if item is already expired
                    categoryId,
                    dbh.getUnit(unitId),
                    dbh.getCategory(categoryId)
                );

                    // Insert the record
                if (dbh.addProduct(p))
                {
                    Toast.makeText(v.getContext(), "Record inserted", Toast.LENGTH_SHORT).show();
                    // Clear text boxes after successful insert
                    et_productName.getText().clear();
                    et_productQuantity.setText("1");
                    et_unitAmount.setText("1");
                    et_expirationDate.setText("");
                    sp_unit.setSelection(0);
                    sp_category.setSelection(0);
                    showNewCategory(false);
                    et_productName.requestFocus();
                }
                else
                {
                    Toast.makeText(v.getContext(), "Insert failed", Toast.LENGTH_SHORT).show();
                }
            }
        });

        // Quantity default value of 1
        et_productQuantity.setOnFocusChangeListener(new View.OnFocusChangeListener()
        {
            public void onFocusChange(View v, boolean hasFocus)
            {
                if (!hasFocus)
                {
                    if (et_productQuantity.getText().toString().isEmpty())       et_productQuantity.setText("1");
                }
            }
        });

        // Unit Amount default value of 1
        et_unitAmount.setOnFocusChangeListener(new View.OnFocusChangeListener()
        {
            public void onFocusChange(View v, boolean hasFocus)
            {
                if (!hasFocus)
                {
                    if (et_unitAmount.getText().toString().isEmpty())       et_unitAmount.setText("1");
                }
            }
        });

        dbh.close();

        return view;
    }

    // Alternate between showing new category textbox and showing category spinner
    private void showNewCategory(boolean show)
    {
        sp_category.setEnabled(!show);
        et_newCategory.setEnabled(show);
        iv_cancelNewCategory.setEnabled(show);

        if (show)
        {
            sp_category.setVisibility(View.GONE);
            et_newCategory.setVisibility(View.VISIBLE);
            iv_cancelNewCategory.setVisibility(View.VISIBLE);
            et_newCategory.requestFocus();
        }
        else        // cancel
        {
            et_newCategory.setVisibility(View.GONE);
            et_newCategory.setText("");
            iv_cancelNewCategory.setVisibility(View.GONE);
            sp_category.setSelection(0);
            sp_category.setVisibility(View.VISIBLE);
        }
    }

    // Creates Spinner-style DatePicker dialog
    private void showDatePickerDialog(final EditText editText)
    {
        DatePickerDialog expDateDialog = new DatePickerDialog
        (
            getContext(),
            R.style.CustomDatePickerDialogTheme,
            new DatePickerDialog.OnDateSetListener()
            {
                @Override
                public void onDateSet(DatePicker datePicker, int year, int month, int dayOfMonth)
                {
                    String date = month + 1 + "/" + dayOfMonth + "/" + year;
                    editText.setText(date);
                    //if (!date.isEmpty())    editText.setError(null);   // Reset input validation error icon
                }
            },
             Calendar.getInstance().get(Calendar.YEAR),
             Calendar.getInstance().get(Calendar.MONTH),
             Calendar.getInstance().get(Calendar.DAY_OF_MONTH)
        );
        expDateDialog.show();
    }

    // However we decide to validate input later on, we can add it here. Or we can add it up above
    // to onCreateView(), but this seemed less cluttered.
    public boolean validateInput()
    {
        boolean valid = true;

        // Check name
        if (TextUtils.isEmpty(et_productName.getText().toString()) )
        {
            et_productName.setError("Name cannot be blank.");
            valid = false;
        }

        // Check quantity
        if (TextUtils.isEmpty(et_productQuantity.getText().toString()) )
        {
            et_productQuantity.setError("Quantity cannot be blank.");
            valid = false;
        }

        // Check new category name
        if (et_newCategory.isEnabled() && TextUtils.isEmpty(et_newCategory.getText().toString()) )
        {
            et_newCategory.setError("Category name cannot be blank.");
            valid = false;
        }

        /* // Check expiration_date
        if (TextUtils.isEmpty(et_expirationDate.getText().toString()) )
        {
            et_expirationDate.setError("Expiration date cannot be blank.");
            valid = false;
        }
         // Check date format (there's probably a more efficient way to do this)
        else
        {
            String dateStr = et_expirationDate.getText().toString();
            Date date = Product.appStr_toDate(dateStr);
            if (date == null)
            {
                et_expirationDate.setError("Invalid date format. (eg. 12/25/2020)");
                valid = false;
            }
            else if (Integer.parseInt(dateStr.substring(0,2)) > 12 || Integer.parseInt(dateStr.substring(3,5)) > 31
                    || (Integer.parseInt(dateStr.substring(6)) < Calendar.getInstance().get(Calendar.YEAR)) )
            {
                et_expirationDate.setError("Invalid date.");
                valid = false;
            }
        }*/

        return valid;
    }

}
