<@extends src="main.ftl">

<#assign screen="theme-options" />

<#assign themeManager=This.getThemeManager()>
<#assign themes=themeManager.getThemeDescriptors()>

<#if !selected_theme_options_category>
  <#assign selected_theme_options_category="background" />
</#if>

<@block name="title">Set theme options</@block>

<@block name="content">

  <table class="nxthemesManageScreen">
  <tr>
    <th style="width: 75%;">Theme options: ${selected_theme_options_category}</th>
    <th style="width: 25%;">Category</th>
  </tr>
  <tr>
  <td>

<#assign presets = This.getCustomPresets(current_theme_name, selected_theme_options_category)>

<form class="nxthemesForm" style="background-color: #f9f9f9; border: 1px solid #eee; padding: 15px"
      onsubmit="NXThemesThemeOptions.updatePresets(this); return false">

    <input type="hidden" name="theme_name" value="${current_theme_name}" />

<#list presets as preset_info>
  <p>
    <label>${preset_info.label}</label>
    <input type="text"
        <#if selected_theme_options_category = 'color'>class="color" style="border-color: #333"</#if>
        id="nxthemes_preset_${preset_info.name}"
        name="preset_${preset_info.name}"
        value="${preset_info.value}" />
    <#if selected_theme_options_category = 'background' | selected_theme_options_category = 'image'>
      <a class="nxthemesActionButton"
         href="javascript:void(0)" onclick="NXThemesEditor.selectEditField('nxthemes_preset_${preset_info.name}', 'image manager')">Browse</a>
    </#if>
    <span class="description">${preset_info.description}</span>
  </p>
</#list>

<#if presets>
  <div>
    <button type="submit">Save</button>
  </div>
<#else>
  <div>No options can be configured in this category.</div>
</#if>

</form>


</td>

  <td>

       <ul class="nxthemesSelector">
         <li <#if selected_theme_options_category = 'background'>class="selected"</#if>>
             <a  href="javascript:NXThemesThemeOptions.selectCategory('background')">
             Background</a></li>
         <li <#if selected_theme_options_category = 'border'>class="selected"</#if>>
             <a  href="javascript:NXThemesThemeOptions.selectCategory('border')">
             Border</a></li>
         <li <#if selected_theme_options_category = 'color'>class="selected"</#if>>
              <a href="javascript:NXThemesThemeOptions.selectCategory('color')">
              Color</a></li>
         <li <#if selected_theme_options_category = 'font'>class="selected"</#if>>
             <a  href="javascript:NXThemesThemeOptions.selectCategory('font')">
             Font</a></li>
         <li <#if selected_theme_options_category = 'image'>class="selected"</#if>>
             <a  href="javascript:NXThemesThemeOptions.selectCategory('image')">
             Image</a></li>
       </ul>

</td>

</tr>
</table>

</@block>
</@extends>