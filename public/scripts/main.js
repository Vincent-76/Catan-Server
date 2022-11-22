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

    $( ".asyncButton" ).click( function( e ) {
        e.preventDefault();
        const href = $( this ).attr( "href" ); // TODO remove
        $.ajax( {
            url: $( this ).attr( "href" ),
            success: replaceDocument,
            error: ( xhr, status, error ) => alert( status + ": " + error )
        } );
    } );

    $( ".asyncForm" ).submit( function( e ) {
        e.preventDefault();
        const url = $( this ).attr( "action" ); // TODO remove
        $.ajax( {
            type: $( this ).attr( "method" ) || "POST",
            url: $( this ).attr( "action" ),
            data: $( this ).serialize(),
            success: replaceDocument,
            error: ( xhr, status, error ) => alert( status + ": " + error )
        } );
    } )

}