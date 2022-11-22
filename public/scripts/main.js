var loading = false;

$( onLoad );

function replaceDocument( html ) {
    const links = $.parseHTML( html.match( /<head[^>]*>([^]*)<\/head/m )[1] ).filter( e => e.nodeName === "LINK" );

    const head = $( "head" );
    const remove = head.children( "link" ).filter( ( i, e ) => {
        const foundIndex = links.findIndex( ne => ne.href === e.href );
        if( foundIndex >= 0 )
            links.splice( foundIndex, 1 );
        return foundIndex < 0;
    } );
    if( links.length === 0 && remove.length === 0 ) {
        head.append( links );
        $( "body" ).html( html.match( /<body[^>]*>([^]*)<\/body/m )[1] );
        remove.remove();
    } else {
        document.open();
        document.write( html );
        document.close();
    }

    onLoad()
}

function onLoad() {

    $( ".asyncButton" ).click( async function( e ) {
        e.preventDefault();
        if( loading ) return;
        loading = true;
        $( this ).addClass( "buttonLoading" )
        await new Promise( resolve => setTimeout( resolve, 3000 ) );
        $.ajax( {
            url: $( this ).attr( "href" ),
            success: replaceDocument,
            error: ( xhr, status, error ) => alert( status + ": " + error ),
            complete: ( data ) => {
                $( this ).removeClass( "buttonLoading" )
                loading = false;
            }
        } );
    } );

    $( ".asyncForm" ).submit( async function( e ) {
        e.preventDefault();
        if( loading ) return;
        loading = true;
        $( this ).find( "input[type=submit]" ).addClass( "buttonLoading" )
        await new Promise( resolve => setTimeout( resolve, 3000 ) );
        $.ajax( {
            type: $( this ).attr( "method" ) || "POST",
            url: $( this ).attr( "action" ),
            data: $( this ).serialize(),
            success: replaceDocument,
            error: ( xhr, status, error ) => alert( status + ": " + error ),
            complete: ( data ) => {
                $( this ).find( "input[type=submit]" ).removeClass( "buttonLoading" )
                loading = false;
            }
        } );
    } )

    $( "#uploadInput" ).change( async function( e ) {
        if( loading ) return;
        loading = true;
        const uploadButton = $( "#uploadButton" )
        uploadButton.addClass( "buttonLoading" )
        await new Promise( resolve => setTimeout( resolve, 3000 ) );
        const fileData = $( this ).prop( "files" )[0];
        const formData = new FormData();
        formData.append( "file", fileData );
        $.ajax( {
            url: $( this ).attr( "href" ),
            //dataType: "html",
            cache: false,
            contentType: false,
            processData: false,
            data: formData,
            type: "POST",
            success: replaceDocument,
            error: ( xhr, status, error ) => alert( status + ": " + error ),
            complete: ( data ) => {
                uploadButton.removeClass( "buttonLoading" )
                loading = false;
            }
        } );
    } );
}