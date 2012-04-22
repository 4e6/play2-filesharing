root = exports ? this

$(document).ready ->
  $('#password, #answer').focus -> cleanValidation $('#hint')
  $('input').keypress (e) ->
    $('#download').trigger 'click' if e.which is 13

root.check = (key) ->
  url = $('#url').val()
  data = $.trim $("##{key}").val()
  file = $('#file').val()
  $.ajax
    url: 'checkSecret'
    type: 'post'
    data: "url=#{url}&#{key}=#{data}"
    dataType: 'json'
    beforeSend: ->
      cleanValidation $()
      $('#loader').show()
    complete: -> $('#loader').hide()
    success: (j) ->
      if j.correct
        setSuccess $('#hint'), "correct"
        getFile(url, file, key, data)
        $('#download').attr 'disabled', 'disabled'
        $('input').attr 'disabled', 'disabled'
      else
        setError $('#hint'), "try again"
    error: (j) -> $('#hint').html j

getFile = (url, file, key, data) ->
  $('#holder').attr "src", "get/#{file}?url=#{url}&#{key}=#{data}"
