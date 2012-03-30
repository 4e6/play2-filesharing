root = exports ? this

$('#password, #answer').focus -> cleanValidation $('#hint')

root.check = (key) ->
  url = $('#url').val()
  data = $.trim $("##{key}").val()
  $.ajax
    url: '__checkSecret'
    type: 'post'
    data:
      'url': url
      'key': key
      'data': data
    dataType: 'json'
    beforeSend: ->
      cleanValidation $()
      $('#loader').show()
    complete: -> $('#loader').hide()
    success: (j) ->
      if j.correct
        setSuccess $('#hint'), "correct"
        getFile(url, key, data)
        # window.location.replace("http://localhost:9000")
        $('#download').attr 'disabled', 'disabled'
      else
        setError $('#hint'), "try again"
    error: (j) -> $('#hint').html j

getFile = (url, key, data) ->
  $('#holder').attr "src", "__retrieve?url=#{url}&key=#{key}&data=#{data}"
