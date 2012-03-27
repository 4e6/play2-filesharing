root = exports ? this

root.check = (key) ->
  url = $('#url').val()
  data = $.trim $("##{key}").val()
  $.ajax
    url: '__checkData'
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
        $('#hint').html "correct"
        getFile(url, key, data)
        # window.location.replace("http://localhost:9000")
        $('#download').attr 'disabled', 'disabled'
      else
        $('#hint').html "try again"
    error: (j) -> $('#hint').html j

getFile = (url, key, data) ->
  $('#holder').attr "src", "__retrieve?url=#{url}&key=#{key}&data=#{data}"
