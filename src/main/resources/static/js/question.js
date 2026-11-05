// Show only the fields that matter for the selected question type
$(function () {
    function showFieldsForType() {
        var type = $('#questionType').val();
        $('#numberFields').toggle(type === 'NUMBER');
        $('#choiceFields').toggle(type === 'CHOICE');
    }

    $('#questionType').change(showFieldsForType);
    showFieldsForType(); // run once when the page loads
});